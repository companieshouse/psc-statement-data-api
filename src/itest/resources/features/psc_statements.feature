Feature: Process Psc Statement Requests

  Scenario Outline: Processing Psc Statement List GET request successfully

    Given Psc statements data api service is running
    And Psc statements exist for company number "<companyNumber>"
    When I send a GET statement list request for company number "<companyNumber>"
    Then I should receive 200 status code
    And the psc statement list Get call response body should match "<result>" file

    Examples:
      | companyNumber | result              |
      | OC421554      | psc_statements_list |

  Scenario Outline: Processing Psc Statement List GET register view request successfully

    Given Psc statements data api service is running
    And Psc statements exist for company number "<companyNumber>"
    And Company Metrics API is available
    And I should receive 200 status code
    When I send a GET statement list request for company number in register view "<companyNumber>"
    Then I should receive 200 status code
    And the psc statement list Get call response body should match "<result>" file

    Examples:
      | companyNumber | result                            |
      | OC421554      | psc_statements_list_register_view |

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
    And Company Metrics API is available
    When I send a GET statement list request for company number in register view "<companyNumber>"
    Then I should receive 404 status code

    Examples:
      | companyNumber |
      | OC421554      |