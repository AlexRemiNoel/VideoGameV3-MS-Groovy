package com.champsoft.gamemanagement.DataAccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional; // Import Optional
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ReviewRepositoryTest {


    @Autowired
    private ReviewRepository reviewRepository;

    // Use meaningful names like review1, review2 instead of game1, game2
    private Review review1;
    private Review review2;
    private ReviewId reviewId1;
    private ReviewId reviewId2;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();

        reviewId1 = new ReviewId(UUID.randomUUID().toString());
        review1 = new Review();
        review1.setReviewId(reviewId1);
        review1.setRating("2");
        review1.setComment("Initial comment for review 1"); // Add comment for completeness

        reviewId2 = new ReviewId(UUID.randomUUID().toString());
        review2 = new Review();
        review2.setReviewId(reviewId2);
        review2.setRating("4");
        review2.setComment("Initial comment for review 2"); // Add comment for completeness

        // Do NOT set the 'game' field here unless you are also saving a Game entity
        // and linking it, which is not what this repository test is focused on.
    }

    @Test
    @DisplayName("Find Review By Existing UUID - Success")
    void whenReviewExists_FindByUuid_ShouldReturnReview() { // Changed method name to reflect Review
        reviewRepository.save(review1);
        String existingUuid = review1.getReviewId().getUuid(); // Use getUuid() from ReviewId

        // FIX: Use standard findById method
        Optional<Review> foundReviewOptional = reviewRepository.findById(new ReviewId(existingUuid));

        assertTrue(foundReviewOptional.isPresent(), "Review should be found"); // Assert that the optional contains a value
        Review foundReview = foundReviewOptional.get(); // Get the Review from the Optional

        assertNotNull(foundReview);
        assertEquals(review1.getReviewId(), foundReview.getReviewId());
        // FIX: Remove assertion about 'game' unless you specifically set and link a Game entity in the setup
        // assertEquals(review1.getGame(), foundReview.getGame()); // This will likely be null == null if not set
        assertEquals(review1.getRating(), foundReview.getRating());
        assertEquals(review1.getComment(), foundReview.getComment()); // Assert comment as well
        assertEquals(existingUuid, foundReview.getReviewId().getUuid());
    }

    @Test
    @DisplayName("Find Review By Non-Existent UUID - Returns Empty Optional") // Changed display name
    void whenReviewDoesNotExist_FindByUuid_ShouldReturnNull() { // Changed method name to reflect Review
        String nonExistentUuid = UUID.randomUUID().toString();

        // FIX: Use standard findById method
        Optional<Review> foundReviewOptional = reviewRepository.findById(new ReviewId(nonExistentUuid));

        assertFalse(foundReviewOptional.isPresent(), "Review should not be found"); // Assert that the optional is empty
    }

    @Test
    @DisplayName("Save New Review - Success") // Changed display name
    void whenSaveNewReview_ShouldPersistReview() { // Changed method name

        Review savedReview = reviewRepository.save(review1); // Changed variable name

        assertNotNull(savedReview);
        assertNotNull(savedReview.getReviewId());
        assertEquals(review1.getReviewId().getUuid(), savedReview.getReviewId().getUuid());
        assertEquals(review1.getRating(), savedReview.getRating()); // Assert other fields too

        // FIX: Use standard findById to retrieve
        Optional<Review> retrievedReviewOptional = reviewRepository.findById(review1.getReviewId());
        assertTrue(retrievedReviewOptional.isPresent(), "Saved review should be retrievable");
        Review retrievedReview = retrievedReviewOptional.get();

        assertEquals(review1.getReviewId(), retrievedReview.getReviewId());
        assertEquals(review1.getRating(), retrievedReview.getRating());
        assertEquals(review1.getComment(), retrievedReview.getComment());
    }

    @Test
    @DisplayName("Find All Reviews - Success") // Changed display name
    void whenMultipleReviewsExist_FindAll_ShouldReturnAllReviews() { // Changed method name
        reviewRepository.save(review1);
        reviewRepository.save(review2);
        long expectedCount = 2;
        List<Review> reviews = reviewRepository.findAll(); // Changed variable name

        assertNotNull(reviews);
        assertEquals(expectedCount, reviews.size());

    }

    @Test
    @DisplayName("Find All Reviews When None Exist - Returns Empty List") // Changed display name
    void whenNoReviewsExist_FindAll_ShouldReturnEmptyList() { // Changed method name
        long expectedCount = 0;

        List<Review> reviews = reviewRepository.findAll(); // Changed variable name

        assertNotNull(reviews);
        assertEquals(expectedCount, reviews.size());
        assertTrue(reviews.isEmpty());
    }


    @Test
    @DisplayName("Delete Review By ID - Success") // Changed display name
    void whenReviewExists_DeleteById_ShouldRemoveReview() { // Changed method name
        Review savedReview = reviewRepository.save(review1); // Changed variable name
        ReviewId idToDelete = savedReview.getReviewId();
        assertTrue(reviewRepository.existsById(idToDelete), "Review should exist before deletion");

        reviewRepository.deleteById(idToDelete);
        assertFalse(reviewRepository.existsById(idToDelete), "Review should not exist after deletion");

        // FIX: Use standard findById and check for empty optional
        Optional<Review> foundAfterDelete = reviewRepository.findById(idToDelete);
        assertFalse(foundAfterDelete.isPresent(), "Finding by ID should return empty optional after deletion");
    }


    @Test
    @DisplayName("Update Existing Review - Success") // Changed display name
    void whenUpdateExistingReview_ShouldReflectChanges() { // Changed method name
        Review savedReview = reviewRepository.save(review1); // Changed variable name
        ReviewId reviewId = savedReview.getReviewId();

        // FIX: Use standard findById to retrieve the entity to update
        Optional<Review> reviewToUpdateOptional = reviewRepository.findById(reviewId);
        assertTrue(reviewToUpdateOptional.isPresent(), "Review to update should be found");
        Review reviewToUpdate = reviewToUpdateOptional.get();

        // Update fields that are actually part of the Review entity
        reviewToUpdate.setRating("5"); // Update rating to "5"
        reviewToUpdate.setComment("Updated comment!"); // Add or update comment

        reviewRepository.save(reviewToUpdate); // Save the updated entity

        // FIX: Use standard findById to retrieve the updated entity for verification
        Optional<Review> updatedReviewOptional = reviewRepository.findById(reviewId);
        assertTrue(updatedReviewOptional.isPresent(), "Updated review should be found");
        Review updatedReview = updatedReviewOptional.get();

        assertEquals(reviewId, updatedReview.getReviewId());
        // FIX: Assert the updated fields
        assertEquals("5", updatedReview.getRating());
        assertEquals("Updated comment!", updatedReview.getComment());
        // FIX: Remove the incorrect assertion about 'Game'
        // assertEquals("Fairy", updatedReview.getGame()); // This assertion is incorrect for a Review entity
    }

    @Test
    @DisplayName("Check Review Existence By ID - Exists") // Changed display name
    void whenReviewExists_ExistsById_ShouldReturnTrue() { // Changed method name
        reviewRepository.save(review1);
        ReviewId existingId = review1.getReviewId();

        boolean exists = reviewRepository.existsById(existingId);

        assertTrue(exists);
    }

    @Test
    @DisplayName("Check Review Existence By ID - Does Not Exist") // Changed display name
    void whenReviewDoesNotExist_ExistsById_ShouldReturnFalse() { // Changed method name
        ReviewId nonExistentId = new ReviewId(UUID.randomUUID().toString());

        boolean exists = reviewRepository.existsById(nonExistentId);

        assertFalse(exists);
    }

    @Test
    @DisplayName("Count Reviews - Success") // Changed display name
    void whenMultipleReviewsExist_Count_ShouldReturnCorrectNumber() { // Changed method name
        reviewRepository.save(review1);
        reviewRepository.save(review2);
        long expectedCount = 2;

        long actualCount = reviewRepository.count();

        assertEquals(expectedCount, actualCount);
    }
}