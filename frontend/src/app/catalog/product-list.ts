import { CurrencyPipe } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Product, ProductApi } from './product-api';

type State = { kind: 'loading' } | { kind: 'error' } | { kind: 'ready'; products: Product[] };

@Component({
  selector: 'app-product-list',
  imports: [CurrencyPipe],
  template: `
    <p class="eyebrow">Baked with care</p>
    <h1>Fresh from our oven</h1>
    <p>Legacy catalog retained during the reconciliation transition.</p>
    <section aria-label="Bakery catalog" [attr.aria-busy]="state().kind === 'loading'">
      @if (state().kind === 'loading') {
        <p role="status">Loading products…</p>
      }
      @if (state().kind === 'error') {
        <div role="alert"><p>We couldn't load the products. Please try again.</p><button type="button" (click)="load()">Try again</button></div>
      }
      @if (ready(); as result) {
        @if (result.products.length === 0) {
          <p role="status">No products are available yet. Please check back later.</p>
        } @else {
          <ul class="products">
            @for (product of result.products; track product.id) {
              <li><article><h2>{{ product.name }}</h2><p>{{ product.description }}</p><p>{{ product.price | currency:"BRL" }}</p>@if (!product.purchasable) {<p>Temporarily unavailable</p>}</article></li>
            }
          </ul>
        }
      }
    </section>
  `
})
export class ProductList {
  private readonly api = inject(ProductApi);
  private readonly destroyRef = inject(DestroyRef);
  readonly state = signal<State>({ kind: 'loading' });
  constructor() { this.load(); }
  ready() { const current = this.state(); return current.kind === 'ready' ? current : null; }
  load() {
    this.state.set({ kind: 'loading' });
    this.api.list().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: products => this.state.set({ kind: 'ready', products }),
      error: () => this.state.set({ kind: 'error' })
    });
  }
}
