package com.musinsa.category.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musinsa.category.dto.CategoryRequest;
import com.musinsa.category.entity.Category;
import com.musinsa.category.enums.Gender;
import com.musinsa.category.repository.CategoryRepository;
import com.musinsa.category.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("성능 테스트")
public class CategoryPerformanceTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @MockBean
    private JwtUtil jwtUtil;

    private String validToken;

    @BeforeEach
    void setUp() {
        // JWT 토큰 모킹
        validToken = "Bearer valid.jwt.token";
        given(jwtUtil.extractTokenFromHeader(validToken)).willReturn("valid.jwt.token");
        given(jwtUtil.isTokenValid("valid.jwt.token")).willReturn(true);
        given(jwtUtil.extractAdminId("valid.jwt.token")).willReturn("admin");
        given(jwtUtil.getExpirationDate("valid.jwt.token")).willReturn(new Date());

        // 테스트 데이터 정리
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("성능 테스트 - 대량 카테고리 조회")
    @Transactional
    void performanceTest_BulkCategory() throws Exception {
        // 대량 테스트 데이터 생성 (100개)
        createBigTestData(100);

        // 조회 성능 측정
        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/categories")
                        .param("gender", "A"))
                .andExpect(status().isOk());

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        // 성능 검증 - 1초 이내 응답
        assertThat(responseTime).isLessThan(1000L);
        System.out.println("대량 데이터 조회 응답 시간: " + responseTime + "ms");
    }

    @Test
    @DisplayName("성능 테스트 - 트리 구조 조회")
    @Transactional
    void performanceTest_CategoryTree() throws Exception {
        // 계층적 테스트 데이터 생성
        createHierarchicalTestData();

        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/categories/tree")
                        .param("gender", "A"))
                .andExpect(status().isOk());

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        // 트리 조회도 1초 이내
        assertThat(responseTime).isLessThan(1000L);
        System.out.println("계층 구조 조회 응답 시간: " + responseTime + "ms");
    }

    @Test
    @DisplayName("동시성 테스트 - 카테고리 생성")
    void concurrencyTest_CategoryCreation() throws Exception {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // 10개 스레드에서 동시에 카테고리 생성
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    CategoryRequest request = CategoryRequest.builder()
                            .name("동시생성테스트" + index)
                            .description("동시성 테스트용 카테고리")
                            .gender(Gender.A)
                            .displayOrder(index + 1)
                            .build();

                    mockMvc.perform(post("/api/categories")
                                    .header("Authorization", validToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                            .andExpect(status().isCreated());

                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    System.err.println("동시성 테스트 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            }, executorService);

            futures.add(future);
        }

        // 모든 작업 완료 대기
        latch.await();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 결과 검증
        System.out.println("성공: " + successCount.get() + ", 실패: " + failureCount.get());

        // 대부분의 요청이 성공해야 함 (최소 70% 이상)
        assertThat(successCount.get()).isGreaterThanOrEqualTo(threadCount * 7 / 10);

        // 실제 데이터베이스에 저장된 개수 확인
        List<Category> createdCategories = categoryRepository.findAll();
        assertThat(createdCategories.size()).isGreaterThanOrEqualTo(successCount.get());

        executorService.shutdown();
    }

    @Test
    @DisplayName("스트레스 테스트 - 조회 요청")
    void stressTest() throws Exception {
        //  테스트 데이터
        createBigTestData(50);

        int requestCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(requestCount);

        AtomicInteger successCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        // 동시에 조회 요청
        for (int i = 0; i < requestCount; i++) {
            executorService.execute(() -> {
                try {
                    mockMvc.perform(get("/api/categories")
                                    .param("gender", "A"))
                            .andExpect(status().isOk());

                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("스트레스 테스트 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        System.out.println("동시 조회 " + requestCount + "회 완료 시간: " + totalTime + "ms");
        System.out.println("성공 요청: " + successCount.get() + "/" + requestCount);

        // 성공하고, 시간이 1초를 넘지 않아야 함
        assertThat(successCount.get()).isEqualTo(requestCount);
        assertThat(totalTime).isLessThan(1000L);

        executorService.shutdown();
    }

    // 대량 데이터 생성
    private void createBigTestData(int count) {
        List<Category> categories = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            Category category = Category.builder()
                    .name("테스트카테고리" + i)
                    .description("대량 테스트용 카테고리")
                    .gender(Gender.A)
                    .depth(1)
                    .displayOrder(i)
                    .path("/테스트카테고리" + i)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .createdBy("admin")
                    .updatedAt(LocalDateTime.now())
                    .updatedBy("admin")
                    .build();
            categories.add(category);
        }

        categoryRepository.saveAll(categories);
    }

    // 트리 테스트 데이터 생성
    private void createHierarchicalTestData() {
        // 루트 카테고리 3개
        for (int i = 1; i <= 3; i++) {
            Category root = Category.builder()
                    .name("루트" + i)
                    .gender(Gender.A)
                    .depth(1)
                    .displayOrder(i)
                    .path("/루트" + i)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .createdBy("admin")
                    .updatedAt(LocalDateTime.now())
                    .updatedBy("admin")
                    .build();

            Category savedRoot = categoryRepository.save(root);

            // 각 루트마다 자식 5개
            for (int j = 1; j <= 5; j++) {
                Category child = Category.builder()
                        .name("자식" + i + "-" + j)
                        .parent(savedRoot)
                        .gender(Gender.A)
                        .depth(2)
                        .displayOrder(j)
                        .path("/루트" + i + "/자식" + i + "-" + j)
                        .isActive(true)
                        .createdAt(LocalDateTime.now())
                        .createdBy("admin")
                        .updatedAt(LocalDateTime.now())
                        .updatedBy("admin")
                        .build();

                categoryRepository.save(child);
            }
        }
    }
}
