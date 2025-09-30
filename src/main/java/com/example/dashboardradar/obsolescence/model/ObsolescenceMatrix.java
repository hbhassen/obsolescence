package com.example.dashboardradar.obsolescence.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record ObsolescenceMatrix(@JsonProperty("frameworks") Map<String, ObsolescenceRule> frameworks) {
}
