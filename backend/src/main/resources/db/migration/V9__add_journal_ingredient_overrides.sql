-- TACHE-518 : permettre la restauration des ajustements d'ingredient
-- lors de l'edition d'une entree journal de type recette.
ALTER TABLE journal_entries
    ADD COLUMN IF NOT EXISTS ingredient_overrides_json TEXT NULL;
