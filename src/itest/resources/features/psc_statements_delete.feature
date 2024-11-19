Feature: Delete statement information

  Scenario Outline: Delete psc statement successfully

    Given Psc statements data api service is running
    And a psc statement exists for company number "<company_number>" with statement id "<id_to_delete>"
    When I send DELETE request for company number "<company_number>" with statement id "<id_to_delete>"
    Then I should receive 200 status code
    And the CHS Kafka API delete is invoked for company number "<company_number>" with statement id "<id_to_delete>" and the correct statement data
    And no statement exists with id "<id_to_delete>"

    Examples:
      | company_number | id_to_delete                |
      | OC421554       | DHTUrJoAuKdXw7zvkreyAm_SoH0 |

  Scenario Outline: Delete psc statement while database is down

    Given Psc statements data api service is running
    And a psc statement exists for company number "<company_number>" with statement id "<id_to_delete>"
    And the database is down
    When I send DELETE request for company number "<company_number>" with statement id "<id_to_delete>"
    Then I should receive 503 status code
    And the CHS Kafka API is not invoked

    Examples:
      | company_number | id_to_delete                |
      | OC421554       | DHTUrJoAuKdXw7zvkreyAm_SoH0 |

  Scenario Outline: Delete psc statement information not found

    Given Psc statements data api service is running
    When I send DELETE request for company number "<company_number>" with statement id "<id_to_delete>"
    And psc statement id does not exist for "<id_to_delete>"
    Then I should receive 200 status code
    And the CHS Kafka API delete is invoked for company number "<company_number>" with statement id "<id_to_delete>" and the correct statement data

    Examples:
      | company_number | id_to_delete   |
      | does_not_exist | does_not_exist |

  Scenario Outline: Delete psc statement when kafka-api is not available

    Given Psc statements data api service is running
    And a psc statement exists for company number "<company_number>" with statement id "<id_to_delete>"
    And CHS kafka API service is unavailable
    When I send DELETE request for company number "<company_number>" with statement id "<id_to_delete>"
    Then I should receive 503 status code
    And a statement exists with id "<id_to_delete>"

    Examples:
      | company_number | id_to_delete                |
      | OC421554       | DHTUrJoAuKdXw7zvkreyAm_SoH0 |

  Scenario Outline: Delete psc statement with stale delta

    Given Psc statements data api service is running
    And a psc statement exists for company number "<company_number>" with statement id "<id_to_delete>" and delta_at "<delta_at>"
    When I send DELETE request for company number "<company_number>" with statement id "<id_to_delete>"
    Then I should receive 409 status code
    And the CHS Kafka API is not invoked
    And a statement exists with id "<id_to_delete>"

    Examples:
      | company_number | id_to_delete                | delta_at             |
      | OC421554       | DHTUrJoAuKdXw7zvkreyAm_SoH0 | 20241023093435661593 |

  Scenario Outline: Delete psc statement with no delta at header

    Given Psc statements data api service is running
    And a psc statement exists for company number "<company_number>" with statement id "<id_to_delete>"
    When I send DELETE request for company number "<company_number>" with statement id "<id_to_delete>" without delta at
    Then I should receive 400 status code
    And the CHS Kafka API is not invoked
    And a statement exists with id "<id_to_delete>"

    Examples:
      | company_number | id_to_delete                |
      | OC421554       | DHTUrJoAuKdXw7zvkreyAm_SoH0 |