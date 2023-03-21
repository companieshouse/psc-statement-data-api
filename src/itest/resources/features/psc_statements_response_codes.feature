Feature: Response codes for psc statements

  Scenario Outline: Processing bad psc statement payload

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