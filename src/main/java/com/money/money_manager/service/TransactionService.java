package com.money.money_manager.service;

import com.money.money_manager.dto.TransactionDTO;
import com.money.money_manager.dto.TransactionSearchDTO;
import com.money.money_manager.entity.Category;
import com.money.money_manager.entity.Transaction;
import com.money.money_manager.entity.User;
import com.money.money_manager.exception.ResourceNotFoundException;
import com.money.money_manager.repository.CategoryRepository;
import com.money.money_manager.repository.TransactionRepository;
import com.money.money_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    
    @Autowired
    private FinancialGoalService financialGoalService;
    
    @Autowired
    private BudgetService budgetService;

    public TransactionDTO createTransaction(Long userId, TransactionDTO transactionDTO) {
        log.info("Creating transaction for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Category category = categoryRepository.findByIdAndUserId(transactionDTO.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Transaction transaction = modelMapper.map(transactionDTO, Transaction.class);
        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setCreatedAt(java.time.LocalDateTime.now());
        transaction.setUpdatedAt(java.time.LocalDateTime.now());

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created successfully with ID: {}", savedTransaction.getId());
        
        // Update financial goals progress
        try {
            financialGoalService.updateGoalProgress(userId, savedTransaction.getType().toString(), savedTransaction.getAmount());
        } catch (Exception e) {
            log.warn("Failed to update goal progress: {}", e.getMessage());
        }
        
        // Update affected budgets
        try {
            budgetService.onTransactionCreated(savedTransaction);
        } catch (Exception e) {
            log.warn("Failed to update budgets: {}", e.getMessage());
        }
        
        return mapToDTO(savedTransaction);
    }

    public TransactionDTO getTransactionById(Long userId, Long transactionId) {
        log.info("Fetching transaction with ID: {}", transactionId);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        
        if (!transaction.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Unauthorized access to transaction");
        }
        
        return mapToDTO(transaction);
    }

    public List<TransactionDTO> getUserTransactions(Long userId) {
        log.info("Fetching transactions for user: {}", userId);
        return transactionRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<TransactionDTO> getTransactionsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching transactions for user {} between {} and {}", userId, startDate, endDate);
        return transactionRepository.findTransactionsBetweenDates(userId, startDate, endDate).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public TransactionDTO updateTransaction(Long userId, Long transactionId, TransactionDTO transactionDTO) {
        log.info("Updating transaction with ID: {}", transactionId);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        
        if (!transaction.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Unauthorized access to transaction");
        }

        if (transactionDTO.getTitle() != null) {
            transaction.setTitle(transactionDTO.getTitle());
        }
        if (transactionDTO.getDescription() != null) {
            transaction.setDescription(transactionDTO.getDescription());
        }
        if (transactionDTO.getAmount() != null) {
            transaction.setAmount(transactionDTO.getAmount());
        }
        if (transactionDTO.getTransactionDate() != null) {
            transaction.setTransactionDate(transactionDTO.getTransactionDate());
        }

        Transaction updatedTransaction = transactionRepository.save(transaction);
        log.info("Transaction updated successfully with ID: {}", transactionId);
        
        // Update affected budgets
        try {
            budgetService.onTransactionUpdated(updatedTransaction);
        } catch (Exception e) {
            log.warn("Failed to update budgets: {}", e.getMessage());
        }
        return mapToDTO(updatedTransaction);
    }

    public void deleteTransaction(Long userId, Long transactionId) {
        log.info("Deleting transaction with ID: {}", transactionId);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        
        if (!transaction.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Unauthorized access to transaction");
        }
        
        // Update affected budgets before deletion
        try {
            budgetService.onTransactionDeleted(transaction);
        } catch (Exception e) {
            log.warn("Failed to update budgets: {}", e.getMessage());
        }
        
        transactionRepository.delete(transaction);
        log.info("Transaction deleted successfully with ID: {}", transactionId);
    }
    
    public Page<TransactionDTO> searchTransactions(Long userId, TransactionSearchDTO searchDTO) {
        log.info("Searching transactions for user: {} with filters: {}", userId, searchDTO);
        
        Sort sort = Sort.by(Sort.Direction.fromString(searchDTO.getSortDirection()), searchDTO.getSortBy());
        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);
        
        Transaction.TransactionType type = null;
        if (searchDTO.getType() != null) {
            type = Transaction.TransactionType.valueOf(searchDTO.getType());
        }
        
        Page<Transaction> transactions = transactionRepository.searchTransactions(
            userId,
            searchDTO.getSearchTerm(),
            searchDTO.getStartDate(),
            searchDTO.getEndDate(),
            searchDTO.getMinAmount(),
            searchDTO.getMaxAmount(),
            type,
            searchDTO.getPaymentMethod(),
            pageable
        );
        
        return transactions.map(this::mapToDTO);
    }
    
    public List<String> getUserTags(Long userId) {
        log.info("Fetching tags for user: {}", userId);
        return transactionRepository.findDistinctTagsByUserId(userId)
                .stream()
                .filter(tags -> tags != null && !tags.trim().isEmpty())
                .flatMap(tags -> Arrays.stream(tags.split(",")))
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    private TransactionDTO mapToDTO(Transaction transaction) {
        TransactionDTO dto = modelMapper.map(transaction, TransactionDTO.class);
        dto.setCategoryName(transaction.getCategory().getName());
        return dto;
    }
}
