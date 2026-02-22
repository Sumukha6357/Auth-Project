import { v4 as uuidv4 } from "uuid";

export function createCorrelationId() {
  return uuidv4();
}
