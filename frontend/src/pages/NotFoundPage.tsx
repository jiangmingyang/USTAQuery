import { Link } from "react-router-dom"
import { Button } from "@/components/ui/button"

export function NotFoundPage() {
  return (
    <div className="container flex flex-col items-center justify-center py-24 text-center">
      <div className="text-6xl mb-4">&#127934;</div>
      <h1 className="text-3xl font-bold mb-2">Page Not Found</h1>
      <p className="text-muted-foreground mb-6">The page you're looking for doesn't exist or has been moved.</p>
      <Link to="/">
        <Button variant="court" size="lg">Back to Home</Button>
      </Link>
    </div>
  )
}