import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListTransaction } from './list-transaction';

describe('ListTransaction', () => {
  let component: ListTransaction;
  let fixture: ComponentFixture<ListTransaction>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListTransaction]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListTransaction);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
