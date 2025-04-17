package com.champsoft.gamemanagement.DataAccess;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Review createTestReview() {
        ReviewId reviewId = new ReviewId(UUID.randomUUID().toString());
        Review review = new Review();
        review.setReviewId(reviewId);
        review.setComment("This game is fantastic!");
        review.setRating("5 stars");
        review.setTimestamp(LocalDateTime.now());
        review.setGame(UUID.randomUUID().toString()); // Assuming it stores Game UUID as String
        return review;
    }

    @Test
    public void whenSaveReview_thenReviewIsPersisted() {
        // Arrange
        Review review = createTestReview();

        // Act
        Review savedReview = reviewRepository.save(review);

        // Assert
        assertNotNull(savedReview.getReviewId());
        assertEquals(review.getComment(), savedReview.getComment());
        assertEquals(review.getRating(), savedReview.getRating());
        assertEquals(review.getTimestamp().truncatedTo(java.time.temporal.ChronoUnit.SECONDS), savedReview.getTimestamp().truncatedTo(java.time.temporal.ChronoUnit.SECONDS)); // Ignore milliseconds
        assertEquals(review.getGame(), savedReview.getGame());

        Optional<Review> foundReview = reviewRepository.findById(savedReview.getReviewId());
        assertTrue(foundReview.isPresent());
        assertEquals(savedReview.getComment(), foundReview.get().getComment());
    }

    @Test
    public void whenFindById_existingId_thenReviewIsReturned() {
        // Arrange
        Review review = createTestReview();
        entityManager.persist(review);
        entityManager.flush();

        // Act
        Optional<Review> foundReview = reviewRepository.findById(review.getReviewId());

        // Assert
        assertTrue(foundReview.isPresent());
        assertEquals(review.getComment(), foundReview.get().getComment());
    }

    @Test
    public void whenFindById_nonExistingId_thenEmptyOptionalIsReturned() {
        // Arrange
        ReviewId nonExistingId = new ReviewId(UUID.randomUUID().toString());

        // Act
        Optional<Review> foundReview = reviewRepository.findById(nonExistingId);

        // Assert
        assertFalse(foundReview.isPresent());
    }

    @Test
    public void whenFindAll_reviewsExist_thenAllReviewsAreReturned() {
        // Arrange
        Review review1 = createTestReview();
        Review review2 = createTestReview();
        entityManager.persist(review1);
        entityManager.persist(review2);
        entityManager.flush();

        // Act
        List<Review> allReviews = reviewRepository.findAll();

        // Assert
        assertEquals(2, allReviews.size());
        assertTrue(allReviews.stream().anyMatch(r -> r.getComment().equals(review1.getComment())));
        assertTrue(allReviews.stream().anyMatch(r -> r.getComment().equals(review2.getComment())));
    }

    @Test
    public void whenFindAll_noReviewsExist_thenEmptyListIsReturned() {
        // Arrange

        // Act
        List<Review> allReviews = reviewRepository.findAll();

        // Assert
        assertTrue(allReviews.isEmpty());
    }

    @Test
    public void whenDeleteById_existingId_thenReviewIsDeleted() {
        // Arrange
        Review review = createTestReview();
        entityManager.persist(review);
        entityManager.flush();

        // Act
        reviewRepository.deleteById(review.getReviewId());

        // Assert
        Optional<Review> deletedReview = reviewRepository.findById(review.getReviewId());
        assertFalse(deletedReview.isPresent());
    }

    @Test
    public void whenDeleteById_nonExistingId_thenNoExceptionIsThrown() {
        // Arrange
        ReviewId nonExistingId = new ReviewId(UUID.randomUUID().toString());

        // Act & Assert
        assertDoesNotThrow(() -> reviewRepository.deleteById(nonExistingId));
    }
}