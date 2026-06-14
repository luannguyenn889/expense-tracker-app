import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TransactionService } from './transaction-service';

describe('ComponentName', () => {
  let component: TransactionService;
  let fixture: ComponentFixture<TransactionService>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TransactionService]
    }).compileComponents();

    fixture = TestBed.createComponent(TransactionService);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});