import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddWallet } from './add-wallet';

describe('AddWallet', () => {
  let component: AddWallet;
  let fixture: ComponentFixture<AddWallet>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddWallet]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddWallet);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
