import { Link, useLocation } from "react-router-dom"
import { Search, Trophy, BarChart3, Sun, Moon } from "lucide-react"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { useState, useEffect } from "react"

const navItems = [
  { to: "/", label: "Home", icon: Search },
  { to: "/tournaments", label: "Tournaments", icon: Trophy },
  { to: "/rankings", label: "Rankings", icon: BarChart3 },
]

export function Header() {
  const location = useLocation()
  const [dark, setDark] = useState(() => document.documentElement.classList.contains("dark"))

  useEffect(() => {
    document.documentElement.classList.toggle("dark", dark)
  }, [dark])

  return (
    <header className="sticky top-0 z-50 border-b bg-background/80 backdrop-blur-lg">
      <div className="container flex h-14 items-center gap-6">
        <Link to="/" className="flex items-center gap-2 font-bold text-lg tracking-tight">
          <div className="h-7 w-7 rounded-md bg-gradient-court flex items-center justify-center">
            <Trophy className="h-4 w-4 text-primary-foreground" />
          </div>
          <span>USTA<span className="text-primary">Query</span></span>
        </Link>

        <nav className="hidden md:flex items-center gap-1">
          {navItems.map(({ to, label, icon: Icon }) => (
            <Link
              key={to}
              to={to}
              className={cn(
                "flex items-center gap-1.5 rounded-md px-3 py-1.5 text-sm font-medium transition-colors",
                location.pathname === to
                  ? "bg-accent text-accent-foreground"
                  : "text-muted-foreground hover:text-foreground hover:bg-accent/50"
              )}
            >
              <Icon className="h-4 w-4" />
              {label}
            </Link>
          ))}
        </nav>

        <div className="ml-auto flex items-center gap-2">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => setDark(!dark)}
            aria-label="Toggle theme"
          >
            {dark ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
          </Button>
        </div>
      </div>
    </header>
  )
}

export function Footer() {
  return (
    <footer className="border-t mt-auto">
      <div className="container flex h-14 items-center justify-between text-xs text-muted-foreground">
        <p>USTA Tennis Query Dashboard</p>
        <p>Data sourced from usta.com</p>
      </div>
    </footer>
  )
}

export function AppShell({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex min-h-screen flex-col">
      <Header />
      <main className="flex-1">{children}</main>
      <Footer />
    </div>
  )
}