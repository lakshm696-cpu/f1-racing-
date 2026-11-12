package com.example.physics

/**
 * Direct implementation of the user's Unity MobileButton MonoBehaviour
 * implementing IPointerDownHandler and IPointerUpHandler.
 */
class MobileButton(
    val input: MobileInput,
    val buttonType: ButtonType
) {
    enum class ButtonType {
        Left, Right, Gas, Brake
    }

    /**
     * Equivalent to OnPointerDown(PointerEventData eventData)
     */
    fun onPointerDown() {
        when (buttonType) {
            ButtonType.Left -> input.LeftDown()
            ButtonType.Right -> input.RightDown()
            ButtonType.Gas -> input.GasDown()
            ButtonType.Brake -> input.BrakeDown()
        }
    }

    /**
     * Equivalent to OnPointerUp(PointerEventData eventData)
     */
    fun onPointerUp() {
        when (buttonType) {
            ButtonType.Left,
            ButtonType.Right -> input.SteeringUp()
            ButtonType.Gas -> input.GasUp()
            ButtonType.Brake -> input.BrakeUp()
        }
    }
}
