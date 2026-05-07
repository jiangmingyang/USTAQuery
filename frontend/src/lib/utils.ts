import { type ClassValue, clsx } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatScore(playerGames: number, opponentGames: number, tiebreakPlayer?: number | null, tiebreakOpponent?: number | null): string {
  let score = `${playerGames}-${opponentGames}`
  if (tiebreakPlayer != null && tiebreakOpponent != null) {
    const loserTb = Math.min(tiebreakPlayer, tiebreakOpponent)
    score += `(${loserTb})`
  }
  return score
}

export function formatDate(dateStr: string | null | undefined): string {
  if (!dateStr) return "-"
  return new Date(dateStr).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" })
}

export function levelColor(level: string): string {
  const map: Record<string, string> = {
    L1: "bg-primary text-primary-foreground",
    L2: "bg-accent text-accent-foreground",
    L3: "bg-secondary text-secondary-foreground",
    L4: "bg-muted text-foreground",
    L5: "bg-muted text-muted-foreground",
    L6: "bg-muted text-muted-foreground",
    L7: "bg-muted text-muted-foreground",
  }
  return map[level] || "bg-muted text-muted-foreground"
}

export function initials(firstName: string, lastName: string): string {
  return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase()
}