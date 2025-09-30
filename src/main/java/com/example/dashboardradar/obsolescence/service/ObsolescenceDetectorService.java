package com.example.dashboardradar.obsolescence.service;

import com.example.dashboardradar.obsolescence.model.ComponentVersion;
import com.example.dashboardradar.obsolescence.model.ObsolescenceFinding;
import java.util.Collection;
import java.util.List;

public interface ObsolescenceDetectorService {

    List<ObsolescenceFinding> evaluate(Collection<ComponentVersion> components);

    String language();
}
