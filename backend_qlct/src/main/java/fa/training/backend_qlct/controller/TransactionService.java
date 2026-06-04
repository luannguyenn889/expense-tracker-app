/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package fa.training.backend_qlct.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Page;

import fa.training.backend_qlct.entities.Transaction;


class TransactionService {

    Page<Transaction> searchTransactions(Long userId, String keyword, LocalDate startDate, LocalDate endDate, String type, Long walletId, Double minAmount, Double maxAmount, int page, int size) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
