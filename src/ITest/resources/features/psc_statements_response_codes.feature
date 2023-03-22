Feature: Response codes for psc statements

  Scenario Outline: Processing bad psc statement payload (should be 400)

    Given Psc statements data api service is running
    When I send a PUT request with payload "<data>" file for company number "<companyNumber>" with statement id "<statementId>"
    Then I should receive <response_code> status code
    And the CHS Kafka API is not invoked
    And nothing is persisted in the database

    Examples:
    | data                      | response_code |
    | bad_company_psc_statement | 500           |


  Scenario: Processing psc statement with no ERIC headers

    Given Psc statements data api service is running
    When I send a PUT request with no ERIC headers
    Then I should receive 403 status code

  Scenario Outline: Put psc statement when kafka-api is not available (Should be 503 and nothing saved to db)


    Given Psc statements data api service is running
    And CHS kafka API service is unavailable
    When I send a PUT request with payload "<data>" file for company number "<companyNumber>" with statement id "<statementId>"
    Then I should receive 200 status code
    And the CHS Kafka API is invoked for company number "<companyNumber>" with statement id "<statementId>"


    Examples:
      | companyNumber | statementId                 | data                  |
      | OC421554      | DHTUrJoAuKdXw7zvkreyAm_SoH0 | company_psc_statement |