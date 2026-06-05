import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListWallet } from './list-wallet';

describe('ListWallet', () => {
  let component: ListWallet;
  let fixture: ComponentFixture<ListWallet>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListWallet]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListWallet);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
