import { type ReactNode } from 'react';
import { ArrowUpRight, FileQuestion, LoaderCircle } from 'lucide-react';

export function cx(...items: Array<string | false | null | undefined>) {
  return items.filter(Boolean).join(' ');
}

export function Button({ children, className, variant = 'primary', loading, type = 'button', disabled, onClick }: {
  children: ReactNode; className?: string; variant?: 'primary' | 'quiet' | 'outline' | 'danger';
  loading?: boolean; type?: 'button' | 'submit'; disabled?: boolean; onClick?: () => void;
}) {
  const styles = {
    primary: 'bg-primary text-primary-foreground hover:brightness-110 shadow-sm',
    quiet: 'bg-secondary text-secondary-foreground hover:bg-secondary/75',
    outline: 'border border-border bg-card text-foreground hover:bg-secondary/55',
    danger: 'bg-destructive text-destructive-foreground hover:brightness-110',
  };
  return <button data-testid={`button-${String(children).toLowerCase().replace(/\s+/g, '-')}`} type={type} disabled={disabled || loading} onClick={onClick} className={cx('inline-flex h-10 items-center justify-center gap-2 rounded-lg px-4 text-sm font-bold transition duration-200 disabled:cursor-not-allowed disabled:opacity-55', styles[variant], className)}>
    {loading && <LoaderCircle size={15} className="animate-spin" />}{children}
  </button>;
}

export function Card({ children, className, ...props }: { children: ReactNode; className?: string; [key: string]: unknown }) {
  return <section className={cx('rounded-2xl border border-border bg-card shadow-[0_10px_30px_-24px_hsl(204_26%_16%/.5)]', className)} {...props}>{children}</section>;
}

export function Badge({ children, tone = 'neutral' }: { children: ReactNode; tone?: 'neutral' | 'teal' | 'orange' | 'red' | 'blue' }) {
  const tones = {
    neutral: 'bg-secondary text-muted-foreground',
    teal: 'bg-primary/10 text-primary',
    orange: 'bg-accent/15 text-accent-foreground',
    red: 'bg-destructive/10 text-destructive',
    blue: 'bg-sky-100 text-sky-800',
  };
  return <span className={cx('inline-flex items-center rounded-full px-2.5 py-1 text-[11px] font-bold uppercase tracking-[.08em]', tones[tone])}>{children}</span>;
}

export function Skeleton({ className }: { className?: string }) {
  return <div className={cx('animate-pulse rounded-lg bg-secondary', className)} aria-label="Loading" />;
}

export function EmptyState({ title, detail, action }: { title: string; detail: string; action?: ReactNode }) {
  return <div className="flex min-h-56 flex-col items-center justify-center rounded-2xl border border-dashed border-border bg-card px-6 text-center">
    <div className="mb-3 rounded-xl bg-secondary p-3 text-muted-foreground"><FileQuestion size={21} /></div>
    <h3 className="font-bold">{title}</h3><p className="mt-1 max-w-sm text-sm text-muted-foreground">{detail}</p>{action && <div className="mt-5">{action}</div>}
  </div>;
}

export function ErrorState({ onRetry, detail = 'The workspace could not load this view.' }: { onRetry?: () => void; detail?: string }) {
  return <div className="rounded-2xl border border-destructive/25 bg-destructive/5 p-6"><p className="font-bold text-destructive">Something needs a second look</p><p className="mt-1 text-sm text-muted-foreground">{detail}</p>{onRetry && <Button variant="outline" onClick={onRetry} className="mt-4">Try again <ArrowUpRight size={15} /></Button>}</div>;
}

export function PageHeader({ eyebrow, title, detail, action }: { eyebrow: string; title: string; detail: string; action?: ReactNode }) {
  return <div className="mb-7 flex flex-col justify-between gap-5 md:flex-row md:items-end"><div><p className="font-mono-ui text-[11px] font-medium uppercase tracking-[.18em] text-primary">{eyebrow}</p><h1 className="mt-2 text-3xl font-extrabold tracking-[-.04em] md:text-4xl">{title}</h1><p className="mt-2 max-w-2xl text-sm text-muted-foreground">{detail}</p></div>{action && <div className="shrink-0">{action}</div>}</div>;
}