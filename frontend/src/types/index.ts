export interface PagedResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface PlayerSummary {
  uaid: string
  firstName: string
  lastName: string
  gender: string
  city: string | null
  state: string | null
  section: string | null
  district: string | null
  ratingNtrp: string | null
  wtnSingles: number | null
  wtnDoubles: number | null
  utrSingles: number | null
}

export interface PlayerDetail extends PlayerSummary {
  sectionCode: string | null
  districtCode: string | null
  nationality: string | null
  itfTennisId: string | null
  ageCategory: string | null
  wheelchair: boolean | null
  wtnSinglesConfidence: number | null
  wtnSinglesLastPlayed: string | null
  wtnSinglesGameZoneUpper: number | null
  wtnSinglesGameZoneLower: number | null
  wtnDoublesConfidence: number | null
  wtnDoublesLastPlayed: string | null
  wtnDoublesGameZoneUpper: number | null
  wtnDoublesGameZoneLower: number | null
  utrId: string | null
  utrDoubles: number | null
  profileImageUrl: string | null
  membershipType: string | null
  membershipExpiry: string | null
}

export interface PlayerStats {
  uaid: string
  totalWins: number
  totalLosses: number
  winPercentage: number
  tournamentsPlayed: number
}

export interface Tournament {
  id: number
  tournamentId: string
  code: string | null
  name: string
  level: string
  category: string
  startDate: string
  endDate: string | null
  entryDeadline: string | null
  acceptingEntries: boolean
  venueName: string | null
  city: string | null
  state: string | null
  section: string | null
  organization: string | null
  orgSlug: string | null
  status: string | null
  eventsCount: number | null
  surface: string | null
  url: string | null
  directorName: string | null
  totalDraws: number | null
  events: TournamentEvent[] | null
}

export interface TournamentEvent {
  eventId: string
  gender: string | null
  eventType: string | null
  ageCategory: string | null
  minAge: number | null
  maxAge: number | null
  surface: string | null
  courtLocation: string | null
  entryFee: number | null
  currency: string | null
  level: string | null
  ballColor: string | null
}

export interface TournamentEntry {
  eventId: string
  participantId: string | null
  playerUaid: string | null
  playerName: string | null
  firstName: string | null
  lastName: string | null
  gender: string | null
  city: string | null
  state: string | null
  eventType: string | null
  entryStage: string | null
  entryStatus: string | null
  entryPosition: number | null
  statusDetail: string | null
  drawId: string | null
  rankingPoints: number | null
}

export interface PlayerTournamentEntry {
  tournamentInternalId: number
  tournamentName: string
  tournamentLevel: string | null
  tournamentCategory: string | null
  startDate: string | null
  endDate: string | null
  city: string | null
  state: string | null
  section: string | null
  eventId: string
  eventType: string | null
  entryStatus: string | null
  entryStage: string | null
  entryPosition: number | null
}

export interface TournamentFilterOptions {
  sections: string[]
  levels: string[]
  genders: string[]
  ageCategories: string[]
  eventTypes: string[]
}

export interface Registration {
  id: number
  tournament: Tournament
  matchType: string
  divisionName: string
  player1: PlayerSummary
  player2: PlayerSummary | null
  seed: number | null
  status: string
  registrationDate: string | null
}

export interface SetScore {
  setNumber: number
  playerGames: number
  opponentGames: number
  tiebreakPlayer: number | null
  tiebreakOpponent: number | null
}

export interface Match {
  id: number
  tournamentName: string
  tournamentId: number
  divisionName: string
  round: string
  matchType: string
  player1: PlayerSummary
  player2: PlayerSummary | null
  opponent1Name: string | null
  opponent1Uaid: string | null
  opponent2Name: string | null
  opponent2Uaid: string | null
  winnerSide: string | null
  winType: string | null
  matchDate: string | null
  scoreSummary: string | null
  durationMinutes: number | null
  sets: SetScore[]
}

export interface Ranking {
  id: number
  playerUaid: string
  playerFirstName: string
  playerLastName: string
  catalogId: string
  displayLabel: string | null
  playerType: string | null
  ageRestriction: string
  ageRestrictionModifier: string | null
  rankListGender: string
  listType: string
  matchFormat: string | null
  matchFormatType: string | null
  familyCategory: string | null
  nationalRank: number | null
  sectionRank: number | null
  districtRank: number | null
  points: number | null
  singlesPoints: number | null
  doublesPoints: number | null
  bonusPoints: number | null
  wins: number | null
  losses: number | null
  trendDirection: string | null
  publishDate: string | null
  section: string | null
  district: string | null
  state: string | null
}

export interface RankingHistory {
  playerUaid: string
  catalogId: string
  displayLabel: string | null
  dataPoints: Ranking[]
}
