import Foundation

struct PagedResponse<T: Codable>: Codable {
    let content: [T]
    let page: Int
    let size: Int
    let totalElements: Int
    let totalPages: Int
    var last: Bool? = nil
}
