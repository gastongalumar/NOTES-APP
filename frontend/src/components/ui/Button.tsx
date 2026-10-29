import type { ButtonHTMLAttributes, ReactNode } from 'react';

type Variant = 'default' | 'primary' | 'ghost' | 'danger';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  small?: boolean;
  children: ReactNode;
}

export function Button({
  variant = 'default',
  small = false,
  className = '',
  children,
  type = 'button',
  ...props
}: ButtonProps) {
  const classes = [
    'button',
    variant === 'primary' ? 'button-primary' : '',
    variant === 'ghost' ? 'button-ghost' : '',
    variant === 'danger' ? 'button-danger' : '',
    small ? 'button-small' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');
  return (
    <button type={type} className={classes} {...props}>
      {children}
    </button>
  );
}
