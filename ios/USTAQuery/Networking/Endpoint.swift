import Foundation

enum Endpoint {
    /// Build a URL from path and optional query parameters.
    /// Nil and empty-string values are filtered out.
    static func url(path: String, query: [String: String?] = [:]) -> URL? {
        let base = AppConfig.baseURL.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        let cleanPath = path.hasPrefix("/") ? path : "/\(path)"
        guard var components = URLComponents(string: base + cleanPath) else { return nil }

        let items = query.compactMap { key, value -> URLQueryItem? in
            guard let v = value, !v.isEmpty else { return nil }
            return URLQueryItem(name: key, value: v)
        }
        if !items.isEmpty {
            components.queryItems = items
        }
        return components.url
    }
}
