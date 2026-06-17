package com.miasi.users.application.ports.inbound;

/**
 * Inbound port: management operations on Assignees and SupportTeams.
 * Triggered via REST or internal calls.
 */
public interface ManageAssigneeUseCase {

    /**
     * Creates a new Assignee user.
     *
     * @param email the agent's email address
     * @return the generated UserID string
     */
    String createAssignee(String email);

    /**
     * Creates a new Requester user.
     *
     * @param email the requester's email address
     * @return the generated UserID string
     */
    String createRequester(String email);

    /**
     * Assigns an agent to a SupportTeam (department).
     *
     * @param agentId    the agent's UserID string
     * @param departmentId the SupportTeamID string
     */
    void assignAgentToDepartment(String agentId, String departmentId);

    /**
     * Removes an agent from a SupportTeam.
     * Corrective policy: called when agent is deleted or suspended.
     *
     * @param agentId    the agent's UserID string
     * @param departmentId the SupportTeamID string
     */
    void removeAgentFromDepartment(String agentId, String departmentId);

    /**
     * Suspends an agent account (also marks agent UNAVAILABLE).
     */
    void suspendAgent(String agentId);

    /**
     * Reactivates a suspended agent.
     */
    void activateAgent(String agentId);

    /**
     * Creates a new SupportTeam with given name and areas of interest.
     *
     * @param name      team name
     * @param areas     comma-separated area names, e.g. "networking,hardware"
     * @return the generated SupportTeamID string
     */
    String createSupportTeam(String name, String areas);
}
