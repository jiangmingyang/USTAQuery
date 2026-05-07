import { useNavigate } from "react-router-dom"
import { SearchBar } from "@/components/shared/SearchBar"
import { Card, CardContent } from "@/components/ui/card"
import { Trophy, Users, BarChart3, Calendar } from "lucide-react"

export function HomePage() {
  const navigate = useNavigate()

  function handleSearch(query: string) {
    navigate(`/search?q=${encodeURIComponent(query)}`)
  }

  return (
    <div>
      {/* Hero Section */}
      <section className="relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-hero" />
        <div
          className="absolute inset-0 opacity-20"
          style={{
            backgroundImage: "url('/hero-tennis-court.png')",
            backgroundSize: "cover",
            backgroundPosition: "center",
            mixBlendMode: "overlay",
          }}
        />
        <div className="relative container py-20 md:py-28">
          <div className="max-w-2xl mx-auto text-center">
            <h1 className="text-4xl md:text-5xl font-extrabold tracking-tight text-primary-foreground mb-4">
              USTA Tennis<br />Tournament Query
            </h1>
            <p className="text-base md:text-lg text-primary-foreground/80 mb-8">
              Search player profiles, rankings, tournament history, match results, and registration records
            </p>
            <SearchBar
              onSearch={handleSearch}
              placeholder="Search by player name or UAID..."
              size="lg"
              className="max-w-lg mx-auto"
            />
          </div>
        </div>
      </section>

      {/* Feature Cards */}
      <section className="container py-12">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <FeatureCard
            icon={Users}
            title="Player Profiles"
            description="WTN, UTR, NTRP ratings and comprehensive player data"
            onClick={() => navigate("/search?q=")}
          />
          <FeatureCard
            icon={Trophy}
            title="Tournaments"
            description="Browse L1-L7 tournaments with filters by level, category, and state"
            onClick={() => navigate("/tournaments")}
          />
          <FeatureCard
            icon={Calendar}
            title="Match Results"
            description="Set-by-set scores with tiebreaks, singles and doubles"
            onClick={() => navigate("/search?q=")}
          />
          <FeatureCard
            icon={BarChart3}
            title="Rankings"
            description="District, Section, and National rankings across all age groups"
            onClick={() => navigate("/rankings")}
          />
        </div>
      </section>

      {/* Quick Stats */}
      <section className="container pb-12">
        <h2 className="text-lg font-semibold mb-4">Quick Access</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
          {["Y12", "Y14", "Y16", "Y18"].map((age) => (
            <Card
              key={`${age}-M`}
              className="cursor-pointer group"
              onClick={() => navigate(`/rankings?list=STANDING&gender=M&ageRestriction=${age}`)}
            >
              <CardContent className="p-4 text-center">
                <p className="text-lg font-bold group-hover:text-primary transition-colors">
                  Boys {age.replace("Y", "")} &amp; Under
                </p>
                <p className="text-xs text-muted-foreground">Rankings</p>
              </CardContent>
            </Card>
          ))}
          {["Y12", "Y14", "Y16", "Y18"].map((age) => (
            <Card
              key={`${age}-F`}
              className="cursor-pointer group"
              onClick={() => navigate(`/rankings?list=STANDING&gender=F&ageRestriction=${age}`)}
            >
              <CardContent className="p-4 text-center">
                <p className="text-lg font-bold group-hover:text-primary transition-colors">
                  Girls {age.replace("Y", "")} &amp; Under
                </p>
                <p className="text-xs text-muted-foreground">Rankings</p>
              </CardContent>
            </Card>
          ))}
        </div>
      </section>
    </div>
  )
}

function FeatureCard({ icon: Icon, title, description, onClick }: {
  icon: React.ElementType; title: string; description: string; onClick: () => void
}) {
  return (
    <Card className="cursor-pointer group" onClick={onClick}>
      <CardContent className="p-5">
        <div className="h-10 w-10 rounded-lg bg-accent flex items-center justify-center mb-3 group-hover:bg-primary/10 transition-colors">
          <Icon className="h-5 w-5 text-accent-foreground group-hover:text-primary transition-colors" />
        </div>
        <h3 className="font-semibold mb-1 group-hover:text-primary transition-colors">{title}</h3>
        <p className="text-sm text-muted-foreground">{description}</p>
      </CardContent>
    </Card>
  )
}