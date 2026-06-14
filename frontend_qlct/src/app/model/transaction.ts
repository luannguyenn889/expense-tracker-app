export type TransactionType = 'INCOME' | 'EXPENSE' | 'TRANSFER';

export class Transaction {
  id: number;
  amount: number;
  note: string;
  transactionDate: string;
  type: TransactionType;
  categoryId: string;
  walletId: number;

  constructor(id: number, amount: number, note: string, transactionDate: string, type: TransactionType, categoryId: string, walletId: number) {
    this.id = id;
    this.amount = amount;
    this.note = note;
    this.transactionDate = transactionDate;
    this.type = type;
    this.categoryId = categoryId;
    this.walletId = walletId;
  }
}

export class TransferData {
  fromWalletId: number | null;
  toWalletId: number | null;
  amount: number;
  note: string;

  constructor(fromWalletId: number | null, toWalletId: number | null, amount: number, note: string) {
    this.fromWalletId = fromWalletId;
    this.toWalletId = toWalletId;
    this.amount = amount;
    this.note = note;
  }
}
