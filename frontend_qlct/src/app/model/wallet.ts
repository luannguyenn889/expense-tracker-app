export class Wallet {
  id: number;
  name: string;
  balance: number;
  description: string;
  currency: string;
  createdAt: string;

  constructor(id: number, name: string, balance: number, description: string, currency: string, createdAt: string) {
    this.id = id;
    this.name = name;
    this.balance = balance;
    this.description = description;
    this.currency = currency;
    this.createdAt = createdAt;
  }
}