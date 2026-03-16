<?php

namespace Nodify\Exception;

class NodifyClientException extends \Exception
{
    private int $statusCode;

    public function __construct(string $message, int $statusCode = -1, ?\Throwable $previous = null)
    {
        $this->statusCode = $statusCode;
        $formattedMessage = $statusCode !== -1
            ? "{$message} (Status: {$statusCode})"
            : $message;
        parent::__construct($formattedMessage, 0, $previous);
    }

    public function getStatusCode(): int
    {
        return $this->statusCode;
    }
}