import { Card, CardContent } from "@/components/ui/card"
import type { PlayerDetail, PlayerStats } from "@/types"
import { initials } from "@/lib/utils"

export function PlayerInfoSection({ player, stats }: { player: PlayerDetail; stats: PlayerStats | null }) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      {/* Profile card */}
      <Card className="md:col-span-1">
        <CardContent className="p-6 flex flex-col items-center text-center">
          <div className="h-20 w-20 rounded-full bg-gradient-court flex items-center justify-center mb-4">
            <span className="text-2xl font-bold text-primary-foreground">
              {initials(player.firstName, player.lastName)}
            </span>
          </div>
          <h2 className="text-xl font-bold">{player.firstName} {player.lastName}</h2>
          <p className="text-sm text-muted-foreground mt-1">
            {player.city && player.state ? `${player.city}, ${player.state}` : player.state || "\u2014"}
          </p>
          <p className="text-xs text-muted-foreground mt-0.5">UAID: {player.uaid}</p>
          {player.section && <p className="text-xs text-muted-foreground">{player.section} · {player.district || ""}</p>}
          {player.nationality && player.nationality !== "USA" && (
            <p className="text-xs text-muted-foreground">{player.nationality}</p>
          )}
          {player.membershipType && (
            <span className="mt-3 inline-flex items-center rounded-md bg-accent px-2.5 py-0.5 text-xs font-medium text-accent-foreground">
              {player.membershipType}
            </span>
          )}
        </CardContent>
      </Card>

      {/* Ratings card */}
      <Card className="md:col-span-1">
        <CardContent className="p-6">
          <h3 className="text-sm font-semibold text-muted-foreground uppercase tracking-wider mb-4">Ratings</h3>
          <div className="space-y-3">
            <RatingRow label="WTN Singles" value={player.wtnSingles} confidence={player.wtnSinglesConfidence} />
            <RatingRow label="WTN Doubles" value={player.wtnDoubles} confidence={player.wtnDoublesConfidence} />
            <RatingRow label="UTR Singles" value={player.utrSingles} />
            <RatingRow label="UTR Doubles" value={player.utrDoubles} />
            <RatingRow label="NTRP" value={player.ratingNtrp} />
          </div>
        </CardContent>
      </Card>

      {/* Stats card */}
      <Card className="md:col-span-1">
        <CardContent className="p-6">
          <h3 className="text-sm font-semibold text-muted-foreground uppercase tracking-wider mb-4">Career Stats</h3>
          {stats ? (
            <div className="grid grid-cols-2 gap-4">
              <StatBlock label="Wins" value={stats.totalWins} className="text-win" />
              <StatBlock label="Losses" value={stats.totalLosses} className="text-loss" />
              <StatBlock label="Tournaments" value={stats.tournamentsPlayed} />
              <div>
                <p className="stat-label">Win Rate</p>
                <p className="stat-value text-primary">
                  {stats.winPercentage > 0 ? stats.winPercentage.toFixed(1) : "0.0"}%
                </p>
              </div>
              <div className="col-span-2">
                <div className="h-2 rounded-full bg-muted overflow-hidden">
                  <div
                    className="h-full rounded-full bg-gradient-court transition-all duration-500"
                    style={{ width: `${stats.winPercentage}%` }}
                  />
                </div>
              </div>
            </div>
          ) : (
            <p className="text-sm text-muted-foreground">No stats available</p>
          )}
        </CardContent>
      </Card>
    </div>
  )
}

function RatingRow({ label, value, confidence }: { label: string; value: number | string | null; confidence?: number | null }) {
  return (
    <div className="flex items-center justify-between">
      <span className="text-sm text-muted-foreground">{label}</span>
      <div className="text-right">
        <span className="font-mono font-semibold text-sm">
          {value != null ? (typeof value === "number" ? value.toFixed(2) : value) : "\u2014"}
        </span>
        {confidence != null && (
          <span className="text-xs text-muted-foreground ml-1">({confidence}%)</span>
        )}
      </div>
    </div>
  )
}

function StatBlock({ label, value, className }: { label: string; value: number; className?: string }) {
  return (
    <div>
      <p className="stat-label">{label}</p>
      <p className={`stat-value ${className || ""}`}>{value}</p>
    </div>
  )
}
