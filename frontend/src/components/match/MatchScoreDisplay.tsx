import type { SetScore } from "@/types"
import { cn } from "@/lib/utils"

interface MatchScoreDisplayProps {
  sets: SetScore[]
  className?: string
}

export function MatchScoreDisplay({ sets, className }: MatchScoreDisplayProps) {
  if (!sets || sets.length === 0) return <span className="text-muted-foreground text-sm">-</span>

  return (
    <div className={cn("flex items-center gap-2", className)}>
      {sets.map((set) => (
        <SetScoreBlock key={set.setNumber} set={set} />
      ))}
    </div>
  )
}

function SetScoreBlock({ set }: { set: SetScore }) {
  const playerWonSet = set.playerGames > set.opponentGames
  const hasTiebreak = set.tiebreakPlayer != null || set.tiebreakOpponent != null
  const loserTb = hasTiebreak
    ? Math.min(set.tiebreakPlayer ?? 99, set.tiebreakOpponent ?? 99)
    : null

  return (
    <span className="score-set animate-score-pop">
      <span className={cn(
        "font-mono",
        playerWonSet ? "text-foreground" : "text-muted-foreground"
      )}>
        {set.playerGames}
      </span>
      <span className="text-muted-foreground">-</span>
      <span className={cn(
        "font-mono",
        !playerWonSet ? "text-foreground" : "text-muted-foreground"
      )}>
        {set.opponentGames}
      </span>
      {loserTb != null && (
        <span className="score-tiebreak">({loserTb})</span>
      )}
    </span>
  )
}

export function WinLossIndicator({ winnerSide }: { winnerSide: string | null }) {
  if (!winnerSide) return null
  return (
    <span className={cn(
      "inline-flex items-center justify-center h-5 w-5 rounded-full text-xs font-bold",
      winnerSide === "PLAYER" ? "bg-win/15 text-win" : "bg-loss/15 text-loss"
    )}>
      {winnerSide === "PLAYER" ? "W" : "L"}
    </span>
  )
}