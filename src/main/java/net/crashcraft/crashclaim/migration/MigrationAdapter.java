package net.crashcraft.crashclaim.migration;

public interface MigrationAdapter {
    /**
     * Make sure everything is in place before the migration continues
     *
     * @return an error message to be displayed and stops the migration
     */
    String checkRequirements(MigrationManager manager);

    /**
     *
     * @param manager supplies needed methods to load data into memory
     * @return an error message if migration failed
     */
    String migrate(MigrationManager manager);

    /**
     * Get an identifier for the migration adapter
     *
     * @return an identifier
     */
    String getIdentifier();
}
