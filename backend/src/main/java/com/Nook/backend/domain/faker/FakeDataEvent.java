package com.Nook.backend.domain.faker;

import java.util.List;

public record FakeDataEvent(
        String type,
        int count,
        List<String> data
) {}