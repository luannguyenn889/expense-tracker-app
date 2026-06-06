import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ListTransaction } from '../../transactions/list-transaction/list-transaction';
import { AddTransaction } from '../../transactions/add-transaction/add-transaction';
import { EditTransaction } from '../../transactions/edit-transaction/edit-transaction';
import { Transfer } from '../../transactions/transfer/transfer';
import { TransactionService } from '../../services/transaction-service';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule, FormsModule, ListTransaction, AddTransaction, EditTransaction, Transfer],
  templateUrl: './transactions.html',
  styleUrl: './transactions.css'
})
export class Transactions implements OnInit {

  transactions: any[] = [];
  wallets: any[] = [];
  allWallets: any[] = [];   
  activeWallets: any[] = [];  
  categories: any[] = [];
  filter = { startDate: '', endDate: '', type: '', walletId: null as number | null };
  currentPage = 0;
  pageSize = 10;
  totalPages = 1;

  showAddModal = false;
  showEditModal = false;
  showTransferModal = false;
  selectedTransaction: any = null;

  constructor(
    private transactionService: TransactionService,
    private cdr: ChangeDetectorRef,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.loadData();
    
    if (sessionStorage.getItem('openAddTransaction') === 'true') {
      sessionStorage.removeItem('openAddTransaction');
      setTimeout(() => {
        this.openAddTransaction();
      }, 500);
    }
  }

  loadData() {
    this.loadWallets();
    this.transactionService.getWallets().subscribe(data => {
      this.wallets = data;
      this.cdr.detectChanges();
    });
    this.transactionService.getCategories().subscribe(data => {
      this.categories = data;
      this.cdr.detectChanges();
    });
    this.loadTransactions();
  }

  loadWallets() {
    this.transactionService.getWallets().subscribe({
      next: (data: any) => {
        this.allWallets = data;        
        this.wallets = data;          
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Lỗi tải ví:', err)
    });
    
    this.http.get(`http://localhost:8080/api/wallets/all?userId=${this.transactionService.getCurrentUserId()}`)
      .subscribe({
        next: (data: any) => {
          this.allWallets = data; 
        },
        error: (err) => console.error('Lỗi tải all ví:', err)
      });
  }

  loadTransactions() {
    this.transactionService.getTransactions(this.currentPage, this.pageSize, this.filter)
      .subscribe(data => {
        let transactions = data.content || [];
        transactions.sort((a: any, b: any) => {
          const dateA = new Date(a.transactionDate);
          const dateB = new Date(b.transactionDate);
          return dateB.getTime() - dateA.getTime();
        });
        this.transactions = transactions;
        this.totalPages = data.totalPages || 1;
        this.cdr.detectChanges();
      });
  }

  search() { 
    this.currentPage = 0; 
    this.loadTransactions(); 
  }
  
  changePage(page: number) { 
    this.currentPage = page; 
    this.loadTransactions(); 
  }

  clearFilter() {
    this.filter = {
      startDate: '',
      endDate: '',
      type: '',
      walletId: null
    };
    this.currentPage = 0;
    this.loadTransactions();
  }

  openAddTransaction() { 
    this.showAddModal = true; 
  }
  
  closeAddModal() { 
    this.showAddModal = false; 
  }

  openEditModal(transaction: any) {
    this.selectedTransaction = transaction;
    this.showEditModal = true;
  }
  
  closeEditModal() { 
    this.showEditModal = false; 
  }

  openTransferModal() { 
    this.showTransferModal = true; 
  }
  
  closeTransferModal() { 
    this.showTransferModal = false; 
  }

  deleteTransaction(id: number) {
    if (confirm('Xóa giao dịch này?')) {
      this.transactionService.deleteTransaction(id).subscribe({
        next: (res: any) => {
          console.log('Delete response:', res);
          this.transactions = this.transactions.filter(t => t.id !== id);
          this.cdr.detectChanges();
          alert('Xóa giao dịch thành công!');
          this.transactionService.getWallets().subscribe(data => {
            this.wallets = data;
            this.cdr.detectChanges();
          });
        },
        error: (err) => {
          console.error('Lỗi chi tiết:', err);
          if (err.status === 200 || err.status === 204) {
            this.transactions = this.transactions.filter(t => t.id !== id);
            this.cdr.detectChanges();
            alert('Xóa giao dịch thành công!');
          } else {
            alert(err.error || 'Xóa giao dịch thất bại!');
          }
        }
      });
    }
  }

  onTransactionAdded() {
    this.closeAddModal();
    this.loadData();
  }

  onTransactionUpdated() {
    this.closeEditModal();
    this.loadData();
  }

  onTransferCompleted() {
    this.closeTransferModal();
    this.loadData();
  }
}