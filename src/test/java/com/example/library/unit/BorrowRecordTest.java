package com.example.library.unit;

import com.example.library.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UNIT TEST - Model Layer
 */
class BorrowRecordTest {

    private Book createSampleBook() {
        Book book = new Book("978-0-13-468599-1", "Clean Code", "Robert C. Martin", 3, Genre.TECHNOLOGY);
        book.setId(1L);
        return book;
    }

    private Member createSampleMember() {
        Member member = new Member("Alice", "alice@example.com", MembershipType.STANDARD);
        member.setId(1L);
        return member;
    }

    // =========================================================================
    // EXAMPLE: calculateFine() tests — filled in as reference
    // =========================================================================

    @Nested
    @DisplayName("calculateFine()")
    class CalculateFineTests {

        @Test
        @DisplayName("should return 0 when book is returned on time")
        void shouldReturnZeroFine_WhenReturnedOnTime() {
            BorrowRecord record = new BorrowRecord(createSampleBook(), createSampleMember());
            record.setReturnDate(record.getDueDate()); // returned exactly on due date

            assertEquals(0.0, record.calculateFine());
        }

        @Test
        @DisplayName("should return 0 when book is returned before due date")
        void shouldReturnZeroFine_WhenReturnedEarly() {
            BorrowRecord record = new BorrowRecord(createSampleBook(), createSampleMember());
            record.setReturnDate(record.getBorrowDate().plusDays(5)); // returned after 5 days

            assertEquals(0.0, record.calculateFine());
        }

        @Test
        @DisplayName("should calculate correct fine when returned 3 days late")
        void shouldCalculateCorrectFine_WhenReturnedLate() {
            BorrowRecord record = new BorrowRecord(createSampleBook(), createSampleMember());
            record.setReturnDate(record.getDueDate().plusDays(3)); // 3 days late

            double expectedFine = 3 * BorrowRecord.DAILY_FINE_RATE; // 3 * 1.50 = 4.50
            assertEquals(expectedFine, record.calculateFine());
        }

        @Test
        @DisplayName("should return 0 when book is not yet returned")
        void shouldReturnZeroFine_WhenNotYetReturned() {
            BorrowRecord record = new BorrowRecord(createSampleBook(), createSampleMember());
            // returnDate is null

            assertEquals(0.0, record.calculateFine());
        }
    }

    // =========================================================================
    // TODO: Students should write these tests
    // =========================================================================

    @Nested
    @DisplayName("isOverdue()")
    class IsOverdueTests {

        @Test
        @DisplayName("should return true when checked after due date and still borrowed")
        void shouldBeOverdue_WhenPastDueDateAndStillBorrowed() {
            //Creates a record that was due yesterday and it is still borrowed
            BorrowRecord record = new BorrowRecord(createSampleBook(), createSampleMember());
            record.setDueDate(LocalDate.now().minusDays(1));
            record.setStatus(BorrowStatus.BORROWED);
            //Checking if it is overdue as of today
            assertTrue(record.isOverdue(LocalDate.now()));
        }

        @Test
        @DisplayName("should return false when checked before due date")
        void shouldNotBeOverdue_WhenBeforeDueDate() {
            //Creates a record with a due date in the future
            BorrowRecord record = new BorrowRecord(createSampleBook(), createSampleMember());
            record.setDueDate(LocalDate.now().plusDays(5));
            //Should not be overdue today
            assertFalse(record.isOverdue(LocalDate.now()));
        }

        @Test
        @DisplayName("should return false when book is already returned (even if past due)")
        void shouldNotBeOverdue_WhenAlreadyReturned() {
            //Record is past due but status is already RETURNED
            BorrowRecord record = new BorrowRecord(createSampleBook(), createSampleMember());
            record.setDueDate(LocalDate.now().minusDays(5));
            record.setStatus(BorrowStatus.RETURNED);
            //Returned books should never be overdue
            assertFalse(record.isOverdue(LocalDate.now()));
        }

        @Test
        @DisplayName("should return false on exactly the due date")
        void shouldNotBeOverdue_OnExactDueDate() {
            //Due date is exactly today
            BorrowRecord record = new BorrowRecord(createSampleBook(), createSampleMember());
            LocalDate today = LocalDate.now();
            record.setDueDate(today);
            //Being on the exact due date is not overdue
            assertFalse(record.isOverdue(today));
        }
    }

    @Nested
    @DisplayName("Constructor / default values")
    class ConstructorTests {

        @Test
        @DisplayName("should set borrow date to today")
        void shouldSetBorrowDateToToday() {
            BorrowRecord record = new BorrowRecord(createSampleBook(), createSampleMember());
            //Checks if borrow date matches current system date
            assertEquals(LocalDate.now(), record.getBorrowDate());
        }

        @Test
        @DisplayName("should set due date to 14 days from today")
        void shouldSetDueDateTo14DaysFromToday() {
            BorrowRecord record = new BorrowRecord(createSampleBook(), createSampleMember());
            LocalDate expectedDueDate = LocalDate.now().plusDays(BorrowRecord.STANDARD_BORROW_DAYS);
            //Verifying the 14-day rule
            assertEquals(expectedDueDate, record.getDueDate());
        }

        @Test
        @DisplayName("should set status to BORROWED")
        void shouldSetStatusToBorrowed() {
            BorrowRecord record = new BorrowRecord(createSampleBook(), createSampleMember());
            //Verifying the default initial status
            assertEquals(BorrowStatus.BORROWED, record.getStatus());
        }
    }
}
