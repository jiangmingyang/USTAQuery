import Foundation

enum AppConfig {
    /// Base URL for the backend API.
    /// Replace <EC2_PUBLIC_IP> with your actual EC2 instance IP after deployment.
    #if DEBUG
    static let baseURL = "http://localhost:8080/api/v1"
    #else
    static let baseURL = "http://<EC2_PUBLIC_IP>/api/v1"
    #endif
}
