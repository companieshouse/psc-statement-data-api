Feature: Delete statement information

  Scenario: Delete psc statement successfully

    Given Psc statements data api service is running
    And a psc statement exists for company number "company_number" with statement id "id_to_delete"
    When I send DELETE request for company number "company_number" with statement id "id_to_delete"
    Then I should receive 200 status code
    And the CHS Kafka API delete is invoked for company number "company_number" with statement id "id_to_delete" and the correct statement data

  Scenario: Delete psc statement while database is down

    Given Psc statements data api service is running
    And a psc statement exists for company number "company_number" with statement id "id_to_delete"
    And the database is down
    When I send DELETE request for company number "company_number" with statement id "id_to_delete"
    Then I should receive 500 status code
    And the CHS Kafka API is not invoked

  Scenario: Delete psc statement information not found

    Given Psc statements data api service is running
    When I send DELETE request for company number "does_not_exist" with statement id "does_not_exist"
    And psc statement id does not exist for "does_not_exist"
    Then I should receive 404 status code
    And the CHS Kafka API is not invoked

  Scenario: Delete psc statement when kafka-api is not available

    Given Psc statements data api service is running
    And a psc statement exists for company number "company_number" with statement id "id_to_delete"
    And CHS kafka API service is unavailable
    When I send DELETE request for company number "company_number" with statement id "id_to_delete"
    Then I should receive 500 status code
    And a statement exists with id "id_to_delete"