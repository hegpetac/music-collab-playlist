import {Component, ElementRef, EventEmitter, Output, QueryList, ViewChildren} from '@angular/core';
import {FormControl, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatFormField} from '@angular/material/form-field';
import {MatButton} from '@angular/material/button';
import {MatInput} from '@angular/material/input';

@Component({
  selector: 'app-code-join',
  imports: [
    MatFormField,
    ReactiveFormsModule,
    FormsModule,
    MatButton,
    MatInput
  ],
  templateUrl: './code-join.html',
  styleUrl: './code-join.css'
})
export class CodeJoin {
  @Output() codeEntered = new EventEmitter<string>();

  public code: string[] = ['', '', '', '', '', '']

  @ViewChildren('codeInput') codeInputs!: QueryList<ElementRef<HTMLInputElement>>;

  public onInput(event: Event, index: number): void {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/\D/g, ''); // only digits

    if (!value) return;

    if (value.length > 1) {
      value.split('').forEach((digit, offset) => {
        const targetIndex = index + offset;
        if (targetIndex < this.code.length) {
          this.code[targetIndex] = digit;
          this.codeInputs.toArray()[targetIndex].nativeElement.value = digit;
        }
      });

      const next = Math.min(index + value.length, this.code.length - 1);
      this.codeInputs.toArray()[next].nativeElement.focus();
      this.emitIfComplete();
      return;
    }

    this.code[index] = value;

    if (index < this.code.length - 1) {
      const nextInput = this.codeInputs.toArray()[index + 1].nativeElement;
      nextInput.value = ''; // clear next box
      this.code[index + 1] = '';
      nextInput.focus();
    }

    this.emitIfComplete();
  }


  public onKeyDown(event: KeyboardEvent, index: number) : void {
    const input = event.target as HTMLInputElement;

    const allowedKeys = ['Backspace', 'ArrowLeft', 'ArrowRight', 'Tab'];

    if (!allowedKeys.includes(event.key) && !/^\d$/.test(event.key)) {
      event.preventDefault();
      return;
    }

    if (event.key === 'Backspace' && !input.value && index > 0) {
      this.codeInputs.toArray()[index - 1].nativeElement.focus();
    }
  }

  public   onFocus(index: number): void {
    const input = this.codeInputs.toArray()[index].nativeElement;
    input.value = '';
    this.code[index] = '';
  }

  public emitIfComplete(): void {
    const joined = this.code.join('');
    if (joined.length == 6 && /^\d{6}$/.test(joined)) {
      this.codeEntered.emit(joined);
    }
  }
}
