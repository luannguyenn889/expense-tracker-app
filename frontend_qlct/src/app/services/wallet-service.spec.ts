import { ComponentFixture, TestBed } from '@angular/core/testing';
import { WalletService } from './wallet-service';

describe('ComponentName', () => {
  let component: WalletService;
  let fixture: ComponentFixture<WalletService>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WalletService]
    }).compileComponents();

    fixture = TestBed.createComponent(WalletService);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});