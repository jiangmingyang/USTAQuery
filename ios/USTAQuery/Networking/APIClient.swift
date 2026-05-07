import Foundation

enum APIClient {

    private static let decoder: JSONDecoder = {
        let d = JSONDecoder()
        return d
    }()

    private static let session: URLSession = {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 30
        return URLSession(configuration: config)
    }()

    // MARK: - Generic fetch

    private static func fetch<T: Decodable>(_ url: URL?) async throws -> T {
        guard let url else { throw APIError.invalidURL }
        do {
            let (data, response) = try await session.data(from: url)
            if let http = response as? HTTPURLResponse, !(200...299).contains(http.statusCode) {
                let body = String(data: data, encoding: .utf8) ?? ""
                throw APIError.httpError(statusCode: http.statusCode, message: body)
            }
            do {
                return try decoder.decode(T.self, from: data)
            } catch {
                throw APIError.decodingError(error)
            }
        } catch let error as APIError {
            throw error
        } catch {
            throw APIError.networkError(error)
        }
    }

    // MARK: - Players

    static func searchPlayers(query: String, page: Int = 0, size: Int = 20) async throws -> PagedResponse<PlayerSummary> {
        try await fetch(Endpoint.url(path: "/players/search", query: [
            "q": query, "page": String(page), "size": String(size)
        ]))
    }

    static func getPlayer(uaid: String) async throws -> PlayerDetail {
        try await fetch(Endpoint.url(path: "/players/\(uaid)"))
    }

    static func getPlayerStats(uaid: String) async throws -> PlayerStats {
        try await fetch(Endpoint.url(path: "/players/\(uaid)/stats"))
    }

    static func getPlayerTournaments(uaid: String, page: Int = 0, size: Int = 20) async throws -> PagedResponse<Tournament> {
        try await fetch(Endpoint.url(path: "/players/\(uaid)/tournaments", query: [
            "page": String(page), "size": String(size)
        ]))
    }

    static func getPlayerTournamentEntries(uaid: String) async throws -> [PlayerTournamentEntry] {
        try await fetch(Endpoint.url(path: "/players/\(uaid)/tournament-entries"))
    }

    static func getPlayerRegistrations(uaid: String, status: String? = nil, page: Int = 0, size: Int = 20) async throws -> PagedResponse<Registration> {
        try await fetch(Endpoint.url(path: "/players/\(uaid)/registrations", query: [
            "status": status, "page": String(page), "size": String(size)
        ]))
    }

    static func getPlayerMatches(uaid: String, page: Int = 0, size: Int = 20) async throws -> PagedResponse<Match> {
        try await fetch(Endpoint.url(path: "/players/\(uaid)/matches", query: [
            "page": String(page), "size": String(size)
        ]))
    }

    static func getPlayerRankings(uaid: String, listType: String? = nil, ageRestriction: String? = nil) async throws -> [Ranking] {
        try await fetch(Endpoint.url(path: "/players/\(uaid)/rankings", query: [
            "listType": listType, "ageRestriction": ageRestriction
        ]))
    }

    static func getPlayerRankingHistory(uaid: String, catalogId: String) async throws -> RankingHistory {
        try await fetch(Endpoint.url(path: "/players/\(uaid)/rankings/history", query: [
            "catalogId": catalogId
        ]))
    }

    // MARK: - Tournaments

    static func searchTournaments(
        q: String? = nil, section: [String]? = nil, level: [String]? = nil,
        state: String? = nil, year: Int? = nil, gender: [String]? = nil,
        ageCategory: [String]? = nil, eventType: [String]? = nil,
        page: Int = 0, size: Int = 20
    ) async throws -> PagedResponse<Tournament> {
        try await fetch(Endpoint.url(path: "/tournaments/search", query: [
            "q": q,
            "section": section?.joined(separator: ","),
            "level": level?.joined(separator: ","),
            "state": state,
            "year": year.map(String.init),
            "gender": gender?.joined(separator: ","),
            "ageCategory": ageCategory?.joined(separator: ","),
            "eventType": eventType?.joined(separator: ","),
            "page": String(page),
            "size": String(size),
        ]))
    }

    static func getTournamentFilters() async throws -> TournamentFilterOptions {
        try await fetch(Endpoint.url(path: "/tournaments/filters"))
    }

    static func getTournament(id: Int) async throws -> Tournament {
        try await fetch(Endpoint.url(path: "/tournaments/\(id)"))
    }

    static func getTournamentEntries(id: Int, eventId: String? = nil) async throws -> [TournamentEntry] {
        try await fetch(Endpoint.url(path: "/tournaments/\(id)/entries", query: [
            "eventId": eventId
        ]))
    }

    // MARK: - Rankings

    static func getLeaderboard(catalogId: String, page: Int = 0, size: Int = 50, publishDate: String? = nil) async throws -> PagedResponse<Ranking> {
        try await fetch(Endpoint.url(path: "/rankings", query: [
            "catalogId": catalogId, "page": String(page), "size": String(size), "publishDate": publishDate
        ]))
    }

    static func getRankingVersions(catalogId: String) async throws -> [String] {
        try await fetch(Endpoint.url(path: "/rankings/versions", query: [
            "catalogId": catalogId
        ]))
    }
}
