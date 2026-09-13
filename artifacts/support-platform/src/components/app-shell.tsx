import { type ReactNode } from 'react';
import { BarChart3, BookOpen, Inbox, LayoutDashboard, LogOut, Menu, Settings, Sparkles, Ticket, X } from 'lucide-react';
import { Link, useLocation } from 'wouter';
import { useState } from 'react';
import { cx } from '@/components/ui';

const nav = [
  { href: '/', label: 'Overview', icon: LayoutDashboard },
  { href: '/tickets', label: 'Tickets', icon: Inbox },
  { href: '/knowledge', label: 'Knowledge base', icon: BookOpen },
];

export function Brand({ light = false }: { light?: boolean }) {
  return <Link href="/" data-testid="link-brand" className={cx('flex items-center gap-3', light ? 'text-sidebar-foreground' : 'text-foreground')}>
    <span className="grid h-9 w-9 place-items-center rounded-xl bg-accent text-accent-foreground shadow-sm"><Sparkles size={17} strokeWidth={2.5} /></span>
    <span><span className="block text-sm font-extrabold tracking-[-.03em]">Northstar</span><span className="block font-mono-ui text-[9px] uppercase tracking-[.16em] opacity-60">support command</span></span>
  </Link>;
}

export function AppShell({ children }: { children: ReactNode }) {
  const [location, setLocation] = useLocation();
  const [open, setOpen] = useState(false);
  const initials = (localStorage.getItem('support_user_name') || 'A').split(' ').map((part) => part[0]).join('').slice(0, 2).toUpperCase();
  const signOut = () => { localStorage.removeItem('support_token'); localStorage.removeItem('support_user_name'); localStorage.removeItem('support_user_email'); localStorage.removeItem('support_user_role'); setLocation('/login'); };
  return <div className="min-h-[100dvh] bg-background">
    <aside className={cx('fixed inset-y-0 left-0 z-40 flex w-[250px] flex-col bg-sidebar px-4 py-5 text-sidebar-foreground transition-transform duration-300 md:translate-x-0', open ? 'translate-x-0' : '-translate-x-full')}>
      <div className="flex items-center justify-between px-2"><Brand light /><button data-testid="button-close-navigation" className="rounded-lg p-2 hover:bg-sidebar-accent md:hidden" onClick={() => setOpen(false)}><X size={18} /></button></div>
      <div className="mt-10 px-2"><p className="font-mono-ui text-[10px] uppercase tracking-[.17em] text-sidebar-foreground/45">Workspace</p><nav className="mt-3 space-y-1">{nav.map(({ href, label, icon: Icon }) => <Link key={href} href={href} onClick={() => setOpen(false)} data-testid={`link-nav-${label.toLowerCase().replace(/\s+/g, '-')}`} className={cx('flex items-center gap-3 rounded-xl px-3 py-3 text-sm font-semibold transition', location === href ? 'bg-sidebar-accent text-sidebar-primary' : 'text-sidebar-foreground/68 hover:bg-sidebar-accent hover:text-sidebar-foreground')}><Icon size={18} />{label}{href === '/tickets' && <span className="ml-auto rounded-md bg-sidebar-primary/15 px-1.5 py-0.5 font-mono-ui text-[10px] text-sidebar-primary">live</span>}</Link>)}</nav></div>
      <div className="mt-auto space-y-1 px-2"><Link href="/settings" onClick={() => setOpen(false)} data-testid="link-nav-settings" className={cx('flex items-center gap-3 rounded-xl px-3 py-3 text-sm font-semibold transition', location === '/settings' ? 'bg-sidebar-accent text-sidebar-primary' : 'text-sidebar-foreground/68 hover:bg-sidebar-accent hover:text-sidebar-foreground')}><Settings size={18} />Settings</Link><button data-testid="button-sign-out" onClick={signOut} className="flex w-full items-center gap-3 rounded-xl px-3 py-3 text-left text-sm font-semibold text-sidebar-foreground/55 transition hover:bg-sidebar-accent hover:text-sidebar-foreground"><LogOut size={18} />Sign out</button></div>
       <div className="mt-7 border-t border-sidebar-border px-2 pt-4"><div className="flex items-center gap-3"><span className="grid h-9 w-9 place-items-center rounded-full bg-sidebar-primary text-xs font-extrabold text-sidebar"><span data-testid="text-user-initials">{initials}</span></span><div className="min-w-0"><p className="truncate text-xs font-bold">{localStorage.getItem('support_user_name') || 'Support operator'}</p><p className="mt-0.5 truncate font-mono-ui text-[10px] text-sidebar-foreground/45">{(localStorage.getItem('support_user_role') || 'AGENT').toLowerCase()} workspace</p></div></div></div>
    </aside>
    {open && <button data-testid="button-close-overlay" aria-label="Close navigation" className="fixed inset-0 z-30 bg-sidebar/40 md:hidden" onClick={() => setOpen(false)} />}
    <div className="md:pl-[250px]"><header className="sticky top-0 z-20 flex h-[72px] items-center justify-between border-b border-border bg-background/90 px-5 backdrop-blur-md md:px-9"><button data-testid="button-open-navigation" className="rounded-lg border border-border bg-card p-2 md:hidden" onClick={() => setOpen(true)}><Menu size={18} /></button><div className="hidden items-center gap-2 md:flex"><BarChart3 size={16} className="text-primary" /><span className="font-mono-ui text-[11px] uppercase tracking-[.12em] text-muted-foreground">Command center / <span className="text-foreground">{location === '/' ? 'overview' : location.slice(1).split('/')[0]}</span></span></div><div className="ml-auto flex items-center gap-3"><div className="hidden items-center gap-2 rounded-full border border-border bg-card px-3 py-1.5 sm:flex"><span className="h-2 w-2 rounded-full bg-primary animate-pulse-line" /><span className="font-mono-ui text-[10px] uppercase tracking-[.12em] text-muted-foreground">Gemini grounded</span></div><Link href="/tickets/new" data-testid="link-new-ticket-header" className="inline-flex items-center gap-2 rounded-lg bg-primary px-3.5 py-2 text-xs font-bold text-primary-foreground shadow-sm transition hover:brightness-110"><Ticket size={14} />New ticket</Link></div></header><main className="mx-auto max-w-[1440px] px-5 py-7 md:px-9 md:py-9">{children}</main></div>
  </div>;
}