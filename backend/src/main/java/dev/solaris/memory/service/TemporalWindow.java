package dev.solaris.memory.service;

import java.time.Instant;

/** An inclusive-from/exclusive-to window used to resolve relative-date expressions like "ontem" against eventAt (RF-12). */
public record TemporalWindow(Instant from, Instant to, String label) {
}
