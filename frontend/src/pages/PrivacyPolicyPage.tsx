export function PrivacyPolicyPage() {
  return (
    <div className="container py-12 max-w-3xl">
      <h1 className="text-3xl font-bold mb-2">Privacy Policy</h1>
      <p className="text-sm text-muted-foreground mb-10">Last updated: June 2025</p>

      <div className="space-y-8 text-sm leading-relaxed text-foreground">

        <section>
          <h2 className="text-lg font-semibold mb-2">1. Overview</h2>
          <p>
            USTAQuery ("we", "our", or "the app") is a read-only tennis tournament information tool
            that aggregates publicly available data from USTA (United States Tennis Association)
            and related sources. This Privacy Policy explains what information we collect, how we
            use it, and your rights.
          </p>
        </section>

        <section>
          <h2 className="text-lg font-semibold mb-2">2. Information We Collect</h2>
          <h3 className="font-medium mt-3 mb-1">2.1 Information You Provide</h3>
          <p>
            We do not require account registration. The app does not collect your name, email
            address, or any personal information to use its features.
          </p>
          <h3 className="font-medium mt-3 mb-1">2.2 Search Queries</h3>
          <p>
            When you search for a player or tournament, the search query is transmitted to our
            backend server to retrieve results. We do not permanently store or log individual
            search queries associated with your identity.
          </p>
          <h3 className="font-medium mt-3 mb-1">2.3 Usage Data</h3>
          <p>
            Our server may log standard technical data such as IP addresses, request timestamps,
            and browser/device type for the purpose of maintaining service stability and security.
            These logs are retained for a limited period and are not sold or shared with third parties.
          </p>
        </section>

        <section>
          <h2 className="text-lg font-semibold mb-2">3. Data Displayed in the App</h2>
          <p>
            USTAQuery displays player profiles, tournament registrations, rankings, and match
            records sourced from publicly available USTA data. This information includes player
            names, USTA IDs, age groups, and tournament participation records. All such data is
            sourced from public-facing USTA systems and is not privately collected by us.
          </p>
          <p className="mt-2">
            If you are a player and believe your information should not be displayed, please
            contact us using the information in Section 7.
          </p>
        </section>

        <section>
          <h2 className="text-lg font-semibold mb-2">4. How We Use Information</h2>
          <ul className="list-disc list-inside space-y-1 text-muted-foreground">
            <li>To provide search and lookup functionality for tennis tournament data</li>
            <li>To maintain and improve the performance and reliability of the service</li>
            <li>To detect and prevent abuse or unauthorized access</li>
          </ul>
          <p className="mt-2">
            We do not use your data for advertising, profiling, or sale to third parties.
          </p>
        </section>

        <section>
          <h2 className="text-lg font-semibold mb-2">5. Cookies and Local Storage</h2>
          <p>
            The web app may store user preferences (such as dark/light mode) in your browser's
            local storage. No tracking cookies or third-party analytics are used.
          </p>
          <p className="mt-2">
            The Android and iOS apps do not use cookies. App preferences are stored locally on
            your device only.
          </p>
        </section>

        <section>
          <h2 className="text-lg font-semibold mb-2">6. Data Security</h2>
          <p>
            We implement reasonable technical measures to protect our backend systems. All
            communication between the app and our servers uses HTTPS encryption. We do not store
            sensitive personal information such as passwords, payment data, or government IDs.
          </p>
        </section>

        <section>
          <h2 className="text-lg font-semibold mb-2">7. Contact</h2>
          <p>
            If you have questions about this Privacy Policy, or wish to request removal of your
            information from our displayed data, please contact us at:
          </p>
          <p className="mt-2 font-medium">
            support@ustaquery.com
          </p>
        </section>

        <section>
          <h2 className="text-lg font-semibold mb-2">8. Changes to This Policy</h2>
          <p>
            We may update this Privacy Policy from time to time. Changes will be posted on this
            page with an updated date. Continued use of the app after changes constitutes
            acceptance of the updated policy.
          </p>
        </section>

      </div>
    </div>
  )
}
