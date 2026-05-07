import { Search } from "lucide-react"
import { useState, type FormEvent } from "react"
import { cn } from "@/lib/utils"

interface SearchBarProps {
  onSearch: (query: string) => void
  placeholder?: string
  defaultValue?: string
  size?: "default" | "lg"
  className?: string
}

export function SearchBar({ onSearch, placeholder = "Search players by name or UAID...", defaultValue = "", size = "default", className }: SearchBarProps) {
  const [query, setQuery] = useState(defaultValue)

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (query.trim()) onSearch(query.trim())
  }

  return (
    <form onSubmit={handleSubmit} className={cn("relative", className)}>
      <Search className={cn(
        "absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground",
        size === "lg" ? "h-5 w-5 left-4" : "h-4 w-4"
      )} />
      <input
        type="text"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        placeholder={placeholder}
        className={cn(
          "w-full rounded-lg border bg-background text-foreground placeholder:text-muted-foreground",
          "focus:outline-none focus:ring-2 focus:ring-ring transition-shadow",
          size === "lg"
            ? "h-14 pl-12 pr-4 text-base"
            : "h-10 pl-10 pr-4 text-sm"
        )}
      />
    </form>
  )
}