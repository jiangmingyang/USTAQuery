import { Loader2 } from "lucide-react"
import { cn } from "@/lib/utils"

export function LoadingSpinner({ className }: { className?: string }) {
  return <Loader2 className={cn("h-6 w-6 animate-spin text-primary", className)} />
}

export function LoadingSection() {
  return (
    <div className="flex items-center justify-center py-16">
      <div className="flex flex-col items-center gap-3">
        <LoadingSpinner className="h-8 w-8" />
        <p className="text-sm text-muted-foreground">Loading data...</p>
      </div>
    </div>
  )
}

export function EmptyState({ title, description }: { title: string; description?: string }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <div className="h-12 w-12 rounded-full bg-muted flex items-center justify-center mb-4">
        <span className="text-2xl">&#127934;</span>
      </div>
      <h3 className="text-lg font-semibold">{title}</h3>
      {description && <p className="text-sm text-muted-foreground mt-1 max-w-md">{description}</p>}
    </div>
  )
}

export function ErrorAlert({ message }: { message: string }) {
  return (
    <div className="rounded-lg border border-destructive/50 bg-destructive/10 p-4 text-sm text-destructive">
      <p className="font-medium">Error</p>
      <p className="mt-1">{message}</p>
    </div>
  )
}

export function StatusBadge({ status }: { status: string }) {
  const colors: Record<string, string> = {
    ENTERED: "bg-accent text-accent-foreground",
    CONFIRMED: "bg-primary/10 text-primary",
    WITHDRAWN: "bg-destructive/10 text-destructive",
    COMPLETED: "bg-muted text-muted-foreground",
  }
  return (
    <span className={cn("inline-flex items-center rounded-md px-2 py-0.5 text-xs font-medium", colors[status] || "bg-muted text-muted-foreground")}>
      {status}
    </span>
  )
}

export function LevelBadge({ level }: { level: string }) {
  // Extract numeric level from strings like "Level 3 Closed", "Level 6", "L3", etc.
  const match = level?.match(/(?:Level\s*|L)(\d)/i)
  const num = match ? parseInt(match[1]) : 0

  return (
    <span className={cn("badge-level", {
      "bg-primary text-primary-foreground": num >= 1 && num <= 2,
      "bg-accent text-accent-foreground": num >= 3 && num <= 4,
      "bg-muted text-foreground": num >= 5 || num === 0,
    })}>
      {level}
    </span>
  )
}

export function CategoryBadge({ category }: { category: string }) {
  return (
    <span className={cn("badge-level", category === "OPEN" ? "bg-primary/10 text-primary" : "bg-secondary text-secondary-foreground")}>
      {category}
    </span>
  )
}