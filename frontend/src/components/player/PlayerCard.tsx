import { Link } from "react-router-dom"
import { Card, CardContent } from "@/components/ui/card"
import type { PlayerSummary } from "@/types"
import { initials } from "@/lib/utils"

export function PlayerCard({ player }: { player: PlayerSummary }) {
  return (
    <Link to={`/players/${player.uaid}`}>
      <Card className="group cursor-pointer transition-all duration-200 hover:border-primary/30">
        <CardContent className="p-4 flex items-center gap-4">
          <div className="h-11 w-11 rounded-full bg-gradient-court flex items-center justify-center flex-shrink-0">
            <span className="text-sm font-bold text-primary-foreground">
              {initials(player.firstName, player.lastName)}
            </span>
          </div>
          <div className="min-w-0 flex-1">
            <p className="font-semibold truncate group-hover:text-primary transition-colors">
              {player.firstName} {player.lastName}
            </p>
            <p className="text-xs text-muted-foreground">
              {player.city && player.state ? `${player.city}, ${player.state}` : player.state || "—"} · UAID: {player.uaid}
            </p>
          </div>
          <div className="hidden sm:flex items-center gap-3 text-right">
            {player.wtnSingles != null && (
              <div>
                <p className="text-xs text-muted-foreground">WTN</p>
                <p className="font-mono font-semibold text-sm">{player.wtnSingles.toFixed(2)}</p>
              </div>
            )}
            {player.ratingNtrp && (
              <div>
                <p className="text-xs text-muted-foreground">NTRP</p>
                <p className="font-mono font-semibold text-sm">{player.ratingNtrp}</p>
              </div>
            )}
          </div>
        </CardContent>
      </Card>
    </Link>
  )
}