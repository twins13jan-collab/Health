-- V108 created the strategy reference tables as the migration
-- owner but never granted the application role access, so every
-- read by app_user failed with permission denied (surfacing as a
-- SQL-grammar 500). Reference data is read-only for the app.
GRANT SELECT ON ref_strategy_priorities TO app_user;
GRANT SELECT ON ref_strategy_goals TO app_user;
