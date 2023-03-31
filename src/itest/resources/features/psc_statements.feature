Feature: Process Psc Statement Requests

  Scenario Outline: Processing Psc Statement GET request successfully

    Given Psc statements data api service is running
    And a psc statement exists for company number "<companyNumber>" with statement id "<statementId>"
    When I send an GET request for company number "<companyNumber>" with statement id "<statementId>"
    Then I should receive 200 status code
    And the psc statement Get call response body should match "<result>" file

    Examples:
      | companyNumber | statementId                 | result        |
      | OC421554      | DHTUrJoAuKdXw7zvkreyAm_SoH0 | psc_statement |


  Scenario Outline: Processing psc statement information successfully

    Given Psc statements data api service is running
    When I send a PUT request with payload "<data>" file for company number "<companyNumber>" with statement id "<statementId>"
    Then I should receive 200 status code
    And the CHS Kafka API is invoked for company number "<companyNumber>" with statement id "<statementId>"
    And a statement exists with id "<statementId>"

    Examples:
      | companyNumber | statementId                 | data                  |
      | OC421554      | DHTUrJoAuKdXw7zvkreyAm_SoH0 | company_psc_statement |


  Scenario Outline: Processing Psc Statement List GET request successfully

    Given Psc statements data api service is running
    And Psc statements exist for company number "<companyNumber>"
    When I send a GET statement list request for company number "<companyNumber>"
    Then I should receive 200 status code
    And the psc statement list Get call response body should match "<result>" file

    Examples:
      | companyNumber |              result          |
      | OC421554      | psc_statements_list_OC421554 |

  Scenario Outline: Processing Psc Statement List GET register view request successfully

    Given Psc statements data api service is running
    And Psc statements exist for company number "<companyNumber>"
    And Company Metrics API is available for company number "<companyNumber>"
    And I should receive 200 status code
    When I send a GET statement list request for company number in register view "<companyNumber>"
    Then I should receive 200 status code
    And the psc statement list Get call response body should match "<result>" file

    Examples:
      | companyNumber |                       result               |
      | OC421554      | psc_statements_list_register_view_OC421554 |

  Scenario Outline: Processing Psc Statement List GET register view request unsuccessfully
    when metrics is unavailable

    Given Psc statements data api service is running
    And Psc statements exist for company number "<companyNumber>"
    And Company Metrics API is unavailable
    When I send a GET statement list request for company number in register view "<companyNumber>"
    Then I should receive 404 status code

    Examples:
      | companyNumber |
      | OC421554      |

  Scenario Outline: Processing Psc Statement List GET register view request unsuccessfully
    when no company psc statements in public register

    Given Psc statements data api service is running
    And nothing is persisted in the database
    And Company Metrics API is available for company number "<companyNumber>"
    When I send a GET statement list request for company number in register view "<companyNumber>"
    Then I should receive 404 status code

    Examples:
      | companyNumber |
      | OC421554      |