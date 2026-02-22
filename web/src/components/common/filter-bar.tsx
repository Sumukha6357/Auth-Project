"use client";

import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Card, CardContent } from "@/components/ui/card";

export function FilterBar({ onSearch }: { onSearch?: (value: string) => void }) {
  const [search, setSearch] = useState("");

  return (
    <Card>
      <CardContent className="flex flex-wrap items-center gap-3 pt-6">
        <Input
          placeholder="Search"
          value={search}
          onChange={(e) => {
            setSearch(e.target.value);
            onSearch?.(e.target.value);
          }}
          className="max-w-xs"
        />
        <Select defaultValue="all">
          <SelectTrigger className="w-[180px]"><SelectValue placeholder="Status" /></SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All statuses</SelectItem>
            <SelectItem value="active">Active</SelectItem>
            <SelectItem value="disabled">Disabled</SelectItem>
          </SelectContent>
        </Select>
        <Input type="date" className="w-[180px]" />
      </CardContent>
    </Card>
  );
}
