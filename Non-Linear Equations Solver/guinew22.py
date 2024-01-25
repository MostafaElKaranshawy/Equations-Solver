import string
from tkinter import *
from tkinter import ttk
import tkinter as tk
import tkinter
import customtkinter
from tkinter.font import Font
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
from matplotlib.backends.backend_tkagg import NavigationToolbar2Tk
from CTkMessagebox import CTkMessagebox
from matplotlib.backend_bases import key_press_handler
from matplotlib.figure import Figure
import matplotlib.pyplot as plt
import sympy as sp
import time
###
#from ttkbootstrap import Style

# print(plt.style.available)
plt.style.use(['dark_background'])

import matplotlib as mpl
import numpy as np
import math
import os
import sys

from sympy import symbols, diff

METHODS = (
"Bisection method", "Regula-falsi method", "Fixed point method", "Secant method", "Newton Raphson's 1st method",
"Newton Raphson's 2nd method")


class Singleton(type):
    _instances = {}

    def __call__(cls, *args, **kwargs):
        if cls not in cls._instances:
            cls._instances[cls] = super(Singleton, cls).__call__(*args, **kwargs)
        return cls._instances[cls]


class MethodFactory(metaclass=Singleton):
    def getMethod(self, methodName):
        if (methodName == None):
            return None
        elif (methodName == "Bisection method"):
            return Bisection()
        elif (methodName == "Regula-falsi method"):
            return RegulaFalsi()
        elif (methodName == "Secant method"):
            return Secant()
        elif (methodName == "Newton Raphson's 1st method"):
            return RaphsonFirstMethod()
        elif (methodName == "Fixed point method"):
            return FixedPt()
        elif (methodName == "Newton Raphson's 2nd method"):
            return RaphsonSecondMethod()
        else:
            return None


# done bisection , false position , fixed point , both newton's methods finallyyy...
class Bisection():

    def __init__(self):
        self.secondPt = None
        self.firstPt = None
        self.equation = None
        self.steps = ''

        ###
        self.toplevel_window = None

        print("im in bisection")
        self.oldValue = 0

    def getPoints(self, root, equation, stepsButton):
        ###
        self.root = root
        self.stepsButton = stepsButton

        frame = Frame(root, bg="#222325")
        frame.pack(fill=tkinter.X)
        self.equation = equation
        self.firstPt = customtkinter.CTkEntry(frame, placeholder_text="First pt.", font=myFont, border_color="#2C74B3")
        self.secondPt = customtkinter.CTkEntry(frame, placeholder_text="Second pt.", font=myFont,
                                               border_color="#2C74B3")
        self.firstPt.pack(fill=tkinter.X, side=tk.LEFT, anchor=NW, expand=True, padx=10, pady=5)
        self.secondPt.pack(fill=tkinter.X, side=tk.RIGHT, anchor=NE, expand=True, padx=10, pady=5)

        mid_frame = Frame(root, bg="#222325")
        mid_frame.pack(fill=tkinter.X)

        self.iterations = customtkinter.CTkEntry(mid_frame, placeholder_text="Iteration number", font=myFont,
                                                 border_color="#2C74B3")
        self.tolerance = customtkinter.CTkEntry(mid_frame, placeholder_text="Tolerance", font=myFont,
                                                border_color="#2C74B3")
        self.percision = customtkinter.CTkEntry(mid_frame, placeholder_text="Percision", font=myFont,
                                                border_color="#2C74B3")
        self.iterations.pack(fill=tkinter.X, side=tk.LEFT, anchor=NW, expand=True, padx=10, pady=5)
        self.tolerance.pack(fill=tkinter.X, side=tk.LEFT, anchor=NW, expand=True, padx=10, pady=5)
        self.percision.pack(fill=tkinter.X, side=tk.LEFT, anchor=NW, expand=True, padx=10, pady=5)
        self.submitPoints = customtkinter.CTkButton(mid_frame, text="Submit", command=self.submitPointsEvent, width=70,
                                                    font=importantFont)
        self.submitPoints.pack(side=tk.RIGHT, anchor=NE, padx=10, pady=5)

    ###
    def showSteps(self):
        if self.toplevel_window is None or not self.toplevel_window.winfo_exists():
            self.toplevel_window = ToplevelWindow(self.root)  # create window if its None or destroyed
        else:
            self.toplevel_window.focus()  # if window exists focus it

        self.toplevel_window.title("Solution Steps")

        self.textbox = customtkinter.CTkTextbox(master=self.toplevel_window, width=400, corner_radius=0)
        self.textbox.pack(fill=tkinter.BOTH, expand=True)
        self.textbox.insert("0.0", self.steps)

    def submitPointsEvent(self):
        ###
        self.stepsButton.configure(state='enabled')
        self.stepsButton.configure(command=self.showSteps)
        self.steps = ''

        try:
            self.firstValue = float(self.firstPt.get())
            self.secondValue = float(self.secondPt.get())
            self.iterationsValue = int(self.iterations.get())
            self.toleranceValue = float(self.tolerance.get())  if self.tolerance.get() else 0.00001
            self.precisionValue = int(self.percision.get()) if self.percision.get() else 7
        except ValueError:
            CTkMessagebox(title="Error", message="Enter valid Input", icon="cancel", font=importantFont)
            return
        startTime = time.time()
        if (self.firstValue > self.secondValue):
            CTkMessagebox(title="Error", message="Enter valid Points", icon="cancel", font=importantFont)
            return
        if ((self.firstValue != None) and (self.secondValue != None) and (self.firstValue != self.secondValue) and (
                self.iterationsValue != None)):
            self.xrOld = (self.firstValue + self.secondValue) / 2.0
            for i in range(1, self.iterationsValue + 1):
                self.xrNew = (self.firstValue + self.secondValue) / 2.0
                if self.xrNew != 0:
                    self.xrNew = round(self.xrNew,
                                       -int(math.floor(math.log10(abs(self.xrNew)))) + (self.precisionValue - 1))
                if abs(self.calculate(self.xrNew)) <= 0:
                    CTkMessagebox(message="Root is equal to " + str(self.xrNew) + f" in {i} iterations", icon="check")
                    if i == 1:
                        self.steps += f"Iteration {i}:\nXᵢ = {self.xrNew}\napproximate  error = ____\n\n\n"
                    endTime = time.time()
                    return
                try:
                    error = math.fabs((self.xrNew - self.xrOld) )
                    if (i > 1):
                        self.steps += f"Iteration {i}:\nXᵢ = {self.xrNew}\napproximate  error = {error}\n\n\n"
                    else:
                        self.steps += f"Iteration {i}:\nXᵢ = {self.xrNew}\napproximate  error = ____\n\n\n"
                except:
                    CTkMessagebox(message="Root is equal to " + str(self.xrNew) + f" in {i} iterations", icon="check")
                    return
                if error < self.toleranceValue and i > 1:
                    endTime = time.time()
                    totalTime = endTime - startTime
                    CTkMessagebox(message="Root is equal to " + str(
                        self.xrNew) + f" in {i} iterations with error = {error}" + f" total time = {totalTime}",
                                  icon="check")
                    return
                elif self.calculate(self.firstValue) * self.calculate(self.xrNew) < 0:
                    self.secondValue = self.xrNew
                else:
                    self.firstValue = self.xrNew
                self.xrOld = self.xrNew

            CTkMessagebox(title="Error", message="Couldn't converge in specified number of iterations, last X = " + str(
                self.xrNew), icon="cancel", font=importantFont)
        else:
            CTkMessagebox(title="Error", message="Enter valid Inputs", icon="cancel", font=importantFont)

    def calculate(self, value):
        return eval(self.equation.replace("e^", "exp").replace("e**", "exp"),
                    {"x": value, "sin": math.sin, "cos": math.cos, "tan": math.tan, "exp": math.exp, "log": math.log,
                     "^": "**"})


class RegulaFalsi():
    def __init__(self):
        self.secondPt = None
        self.steps = ''
        self.firstPt = None
        self.toplevel_window = None
        self.equation = None
        print("im in bisection")
        self.oldValue = 0

    def getPoints(self, root, equation, stepsButton):

        self.root = root
        self.stepsButton = stepsButton

        frame = Frame(root, bg="#222325")
        frame.pack(fill=tkinter.X)
        self.equation = equation
        self.firstPt = customtkinter.CTkEntry(frame, placeholder_text="First pt.", font=myFont, border_color="#2C74B3")
        self.secondPt = customtkinter.CTkEntry(frame, placeholder_text="Second pt.", font=myFont,
                                               border_color="#2C74B3")
        self.firstPt.pack(fill=tkinter.X, side=tk.LEFT, anchor=NW, expand=True, padx=10, pady=5)
        self.secondPt.pack(fill=tkinter.X, side=tk.RIGHT, anchor=NE, expand=True, padx=10, pady=5)

        mid_frame = Frame(root, bg="#222325")
        mid_frame.pack(fill=tkinter.X)

        self.iterations = customtkinter.CTkEntry(mid_frame, placeholder_text="Iteration number", font=myFont,
                                                 border_color="#2C74B3")
        self.tolerance = customtkinter.CTkEntry(mid_frame, placeholder_text="Tolerance", font=myFont,
                                                border_color="#2C74B3")
        self.percision = customtkinter.CTkEntry(mid_frame, placeholder_text="Percision", font=myFont,
                                                border_color="#2C74B3")
        self.iterations.pack(fill=tkinter.X, side=tk.LEFT, anchor=NW, expand=True, padx=10, pady=5)
        self.tolerance.pack(fill=tkinter.X, side=tk.LEFT, anchor=NW, expand=True, padx=10, pady=5)
        self.percision.pack(fill=tkinter.X, side=tk.LEFT, anchor=NW, expand=True, padx=10, pady=5)
        self.submitPoints = customtkinter.CTkButton(mid_frame, text="Submit", command=self.submitPointsEvent, width=70,
                                                    font=importantFont)
        self.submitPoints.pack(side=tk.RIGHT, anchor=NE, padx=10, pady=5)

    def showSteps(self):
        if self.toplevel_window is None or not self.toplevel_window.winfo_exists():
            self.toplevel_window = ToplevelWindow(self.root)  # create window if its None or destroyed
        else:
            self.toplevel_window.focus()  # if window exists focus it

        self.toplevel_window.title("Solution Steps")

        self.textbox = customtkinter.CTkTextbox(master=self.toplevel_window, width=400, corner_radius=0)
        self.textbox.pack(fill=tkinter.BOTH, expand=True)
        self.textbox.insert("0.0", self.steps)

    def submitPointsEvent(self):
        self.steps = ''
        self.stepsButton.configure(state='enabled')
        self.stepsButton.configure(command=self.showSteps)
        try:
            self.firstValue = float(self.firstPt.get())
            self.secondValue = float(self.secondPt.get())
            self.iterationsValue = int(self.iterations.get())
            self.toleranceValue = float(self.tolerance.get())  if self.tolerance.get() else 0.00001
            self.precisionValue = int(self.percision.get()) if self.percision.get() else 7
        except ValueError:
            CTkMessagebox(title="Error", message="Enter valid Input", icon="cancel", font=importantFont)
            return
        startTime = time.time()
        if (self.firstValue > self.secondValue):
            CTkMessagebox(title="Error", message="Enter valid Points", icon="cancel", font=importantFont)
            return
        if ((self.firstValue != None) and (self.secondValue != None) and (self.firstValue != self.secondValue) and (
                self.iterationsValue != None)):
            self.xrOld = (self.firstValue * self.calculate(self.secondValue) - self.secondValue * self.calculate(
                self.firstValue)) / (self.calculate(self.secondValue) - self.calculate(self.firstValue))
            for i in range(1, self.iterationsValue + 1):
                self.xrNew = (self.firstValue * self.calculate(self.secondValue) - self.secondValue * self.calculate(
                    self.firstValue)) / (self.calculate(self.secondValue) - self.calculate(self.firstValue))
                if self.xrNew != 0:
                    self.xrNew = round(self.xrNew,
                                       -int(math.floor(math.log10(abs(self.xrNew)))) + (self.precisionValue - 1))
                if abs(self.calculate(self.xrNew)) <= 0:
                    endTime = time.time()
                    totalTime = endTime - startTime
                    CTkMessagebox(message="Root is equal to " + str(
                        self.xrNew) + f" in {i} iterations" + f" with total time = {totalTime}", icon="check")
                    if i == 1:
                        self.steps += f"Iteration {i}:\nXᵢ = {self.xrNew}\napproximate  error = ____\n\n\n"
                    return
                try:
                    error = math.fabs((self.xrNew - self.xrOld))
                    if (i > 1):
                        self.steps += f"Iteration {i}:\nXᵢ = {self.xrNew}\napproximate  error = {error}\n\n\n"
                    else:
                        self.steps += f"Iteration {i}:\nXᵢ = {self.xrNew}\napproximate  error = ____\n\n\n"
                except:
                    endTime = time.time()
                    totalTime = endTime - startTime
                    CTkMessagebox(message="Root is equal to " + str(
                        self.xrNew) + f" in {i} iterations" + f" with total time = {totalTime}", icon="check")
                    return
                if error < self.toleranceValue and i > 1:
                    endTime = time.time()
                    totalTime = endTime - startTime
                    CTkMessagebox(message="Root is equal to " + str(
                        self.xrNew) + f" in {i} iterations with error = {error}" + f" with total time = {totalTime}",
                                  icon="check")
                    return
                elif self.calculate(self.firstValue) * self.calculate(self.xrNew) < 0:
                    self.secondValue = self.xrNew
                else:
                    self.firstValue = self.xrNew
                self.xrOld = self.xrNew

            CTkMessagebox(title="Error", message="Couldn't converge in specified number of iterations, last X = " + str(
                self.xrNew), icon="cancel", font=importantFont)
        else:
            CTkMessagebox(title="Error", message="Enter valid Input", icon="cancel", font=importantFont)

    def calculate(self, value):
        return eval(self.equation.replace("e^", "exp").replace("e**", "exp"),
                    {"x": value, "sin": math.sin, "cos": math.cos, "tan": math.tan, "exp": math.exp, "log": math.log,
                     "^": "**"})


class FixedPt():

    def __init__(self):
        print("im in fixed pt.")
        self.steps = ''
        self.toplevel_window = None
        self.iterationsValue = None
        self.fixedPtValue = None
        pass

    def getPoints(self, root, equation, stepsButton):

        self.root = root
        self.stepsButton = stepsButton

        frame = Frame(root, bg="#222325")
        frame.pack(fill=tkinter.X)
        self.equation = equation
        self.fixedPt = customtkinter.CTkEntry(frame, placeholder_text="Fixed pt.", font=myFont, border_color="#2C74B3")
        new_frame = Frame(root, bg="#222325")
        new_frame.pack(fill=tkinter.X)
        self.iterations = customtkinter.CTkEntry(new_frame, placeholder_text="Iteration number", font=myFont,
                                                 border_color="#2C74B3")
        self.tolerance = customtkinter.CTkEntry(new_frame, placeholder_text="Tolerance", font=myFont,
                                                border_color="#2C74B3")
        self.percision = customtkinter.CTkEntry(new_frame, placeholder_text="Percision", font=myFont,
                                                border_color="#2C74B3")
        self.fixedPt.pack(fill=tkinter.X, side=tkinter.LEFT, anchor=NW, expand=True, padx=10, pady=5)
        self.iterations.pack(fill=tkinter.X, side=tkinter.LEFT, anchor=tkinter.NW, expand=True, padx=10, pady=5)
        self.tolerance.pack(fill=tkinter.X, side=tkinter.LEFT, anchor=NW, expand=True, padx=10, pady=5)
        self.percision.pack(fill=tkinter.X, side=tkinter.LEFT, anchor=tkinter.NW, expand=True, padx=10, pady=5)

        self.submitPoints = customtkinter.CTkButton(new_frame, text="Submit", command=self.submitPointsEvent, width=70,
                                                    font=importantFont)
        self.submitPoints.pack(padx=10, pady=5)

    def submitPointsEvent(self):
        try:
            self.fixedPtValue = float(self.fixedPt.get())
            self.iterationsValue = int(self.iterations.get())
            self.toleranceValue = float(self.tolerance.get())  if self.tolerance.get() else 0.00001
            self.percisionValue = int(self.percision.get()) if self.percision.get() else 7
        except ValueError:
            CTkMessagebox(title="Error", message="Enter valid Input", icon="cancel", font=importantFont)

        if self.fixedPtValue is not None:
            self.performFixedPointIteration()
        else:
            CTkMessagebox(title="Error", message="Enter valid initial Guess", icon="cancel", font=importantFont)

    def showSteps(self):
        if self.toplevel_window is None or not self.toplevel_window.winfo_exists():
            self.toplevel_window = ToplevelWindow(self.root)  # create window if its None or destroyed
        else:
            self.toplevel_window.focus()  # if window exists focus it

        self.toplevel_window.title("Solution Steps")

        self.textbox = customtkinter.CTkTextbox(master=self.toplevel_window, width=400, corner_radius=0)
        self.textbox.pack(fill=tkinter.BOTH, expand=True)
        self.textbox.insert("0.0", self.steps)

    def performFixedPointIteration(self):
        self.steps = ''
        self.stepsButton.configure(state='enabled')
        self.stepsButton.configure(command=self.showSteps)
        startTime = time.time()
        x_old = self.fixedPtValue
        if (self.fixedPtValue != None) and (self.iterationsValue != None):
            for i in range(1, self.iterationsValue + 1):
                try:
                    if x_old != 0:
                        x_new = round(self.calculate(x_old),
                                      -int(math.floor(math.log10(abs(self.calculate(x_old))))) + (self.percisionValue - 1))
                except OverflowError as e:
                    self.steps += f"Iteration {i}:\nXᵢ = {x_new}\n overflow occurred\n\n\n"
                    CTkMessagebox(title="Error",
                                  message="overflow occurred, the function diverged",
                                  icon="cancel",
                                  font=importantFont)
                    return


                error = abs((x_new - x_old))
                self.steps += f"Iteration {i}:\nXᵢ = {x_new}\napproximate  error = {error}\n\n\n"
                print(error)
                if error < self.toleranceValue:
                    endTime = time.time()
                    totalTime = endTime - startTime
                    CTkMessagebox(
                        message=f"Root is approximately equal to {x_new} with error {error} and in {i} iterations" + f" with total time = {totalTime}",
                        icon="check")
                    derivative_at_x_old = self.calculate_derivative(self.fixedPtValue)
                    if math.fabs(derivative_at_x_old) >= 1:
                        CTkMessagebox(message="Fixed Point Iterative Method may diverge as |g'(x)| > 1", icon="cancel",
                                      font=importantFont)
                    return
                x_old = x_new
            endTime = time.time()
            totalTime = endTime - startTime
            CTkMessagebox(title="Error",
                          message=f"Couldn't converge within the specified iterations until reached x = {x_new} with error tolerance {error}" + f" with total time = {totalTime}",
                          icon="cancel",
                          font=importantFont)
            derivative_at_x_old = self.calculate_derivative(self.fixedPtValue)
            if math.fabs(derivative_at_x_old) >= 1:
                CTkMessagebox(message="Fixed Point Iterative Method may diverge as |g'(x)| > 1", icon="cancel",
                              font=importantFont)

    def calculate_derivative(self, value):
        x = symbols('x')
        expr = eval(self.equation.replace('x', 'x'))
        derivative = diff(expr, x)
        return derivative.subs(x, value)

    def calculate(self, value):
        return eval(self.equation.replace("e^", "exp").replace("e**", "exp"),
                    {"x": value, "sin": math.sin, "cos": math.cos, "tan": math.tan, "exp": math.exp, "log": math.log,
                     "^": "**"})


class Secant():
    def __init__(self):
        self.toplevel_window = None
        self.steps = ''
        self.iterationsValue = None
        self.firstPointValue = None
        self.secondPointValue = None
        print("im in secant")
        pass

    def getPoints(self, root, equation, stepsButton):
        self.root = root
        self.stepsButton = stepsButton
        frame = Frame(root, bg="#222325")
        frame.pack(fill=tkinter.X)

        self.equation = equation
        self.firstPoint = customtkinter.CTkEntry(frame, placeholder_text="X-1", font=myFont, border_color="#2C74B3")
        self.secondPoint = customtkinter.CTkEntry(frame, placeholder_text="Xo", font=myFont, border_color="#2C74B3")
        self.firstPoint.pack(fill=tkinter.X, side=tk.LEFT, anchor=NW, expand=True, padx=10, pady=5)
        self.secondPoint.pack(fill=tkinter.X, side=tk.LEFT, anchor=NW, expand=True, padx=10, pady=5)

        new_frame = Frame(root, bg="#222325")
        new_frame.pack(fill=tkinter.X)
        self.iterations = customtkinter.CTkEntry(new_frame, placeholder_text="Iteration number", font=myFont,
                                                 border_color="#2C74B3")
        self.tolerance = customtkinter.CTkEntry(new_frame, placeholder_text="Tolerance", font=myFont,
                                                border_color="#2C74B3")
        self.percision = customtkinter.CTkEntry(new_frame, placeholder_text="Percision", font=myFont,
                                                border_color="#2C74B3")
        self.iterations.pack(fill=tkinter.X, side=tk.LEFT, anchor=NE, expand=True, padx=10, pady=5)
        self.tolerance.pack(fill=tkinter.X, side=tk.LEFT, anchor=NW, expand=True, padx=10, pady=5)
        self.percision.pack(fill=tkinter.X, side=tk.LEFT, anchor=NE, expand=True, padx=10, pady=5)
        self.submitPoints = customtkinter.CTkButton(new_frame, text="Submit", command=self.submitPointsEvent, width=70,
                                                    font=importantFont)
        self.submitPoints.pack(padx=10, pady=5)

    def showSteps(self):
        if self.toplevel_window is None or not self.toplevel_window.winfo_exists():
            self.toplevel_window = ToplevelWindow(self.root)  # create window if its None or destroyed
        else:
            self.toplevel_window.focus()  # if window exists focus it

        self.toplevel_window.title("Solution Steps")

        self.textbox = customtkinter.CTkTextbox(master=self.toplevel_window, width=400, corner_radius=0)
        self.textbox.pack(fill=tkinter.BOTH, expand=True)
        self.textbox.insert("0.0", self.steps)

    def submitPointsEvent(self):
        try:
            self.firstPointValue = float(self.firstPoint.get())
            self.secondPointValue = float(self.secondPoint.get())
            self.iterationsValue = int(self.iterations.get())
            self.toleranceValue = float(self.tolerance.get()) if self.tolerance.get() else 0.00001
            self.percisionValue = int(self.percision.get()) if self.percision.get() else 7
        except ValueError:
            CTkMessagebox(title="Error", message="Enter valid Input", icon="cancel", font=importantFont)

        if self.firstPointValue is not None and self.secondPointValue is not None and self.iterationsValue is not None \
                and self.toleranceValue is not None and self.percisionValue is not None:
            self.performSecant()

    def performSecant(self):
        self.steps = ''
        self.stepsButton.configure(state='enabled')
        self.stepsButton.configure(command=self.showSteps)
        root = 0.0;
        iter = 1
        error = 0.0;
        x_old = self.firstPointValue;
        x_new = self.secondPointValue
        startTime = time.time()
        if (abs(self.f(x_new)) == 0):
                endTime = time.time()
                totalTime = endTime - startTime
                CTkMessagebox(
                    message=f"Root is exactly equal to {x_new} with error ____ and in {0} iterations" + f" with total time = {totalTime}",
                    icon="check")
                return
        while iter < self.iterationsValue + 1:
            if self.f(x_old) - self.f(x_new) == 0:
                endTime = time.time()
                totalTime = endTime - startTime
                CTkMessagebox(title="Error", message="method diverage due to subtruction cancelation " +f" with total time = {totalTime} ", icon="cancel",
                              font=importantFont)
                return
            root = self.significantFigure(self.significantFigure(x_new) - self.significantFigure(self.f(x_new)) * (
                self.significantFigure(x_new - x_old)) / self.significantFigure(-self.f(x_old) + self.f(x_new)))
            # root = x_new - self.f(x_new) * (x_new - x_old) / (self.f(x_new) - self.f(x_old))
            print(root)
            root = self.significantFigure(root)
            print(root)
            error = self.significantFigure(abs((root - x_new)))

            self.steps += f"Iteration {iter}:\nXi-1 = {x_old}\nXi = {x_new}\nroot = {root}\n\napproximate  error = {error}\n\n\n"
            # root is exact
            if self.f(root) == 0:
                endTime = time.time()
                totalTime = endTime - startTime
                CTkMessagebox(
                    message=f"Root is exactly equal to {root} with error {error} and in {iter} iterations" + f" with total time = {totalTime}",
                    icon="check")
                return
            # error < tolerance
            if error < self.toleranceValue:
                endTime = time.time()
                totalTime = endTime - startTime
                CTkMessagebox(
                    message=f"Root is approximately equal to {root} with error {error} and in {iter} iterations" + f" with total time = {totalTime}",
                    icon="check")
                return

            x_old = x_new
            x_new = root

            iter += 1
        endTime = time.time()
        totalTime = endTime - startTime
        CTkMessagebox(title="Error",
                      message=f"Couldn't converge within the specified iterations until reached x = {root} with error tolerance {error}"f" with total time = {totalTime}",
                      icon="cancel",
                      font=importantFont)
        return

    def f(self, value):
        return eval(self.equation.replace("e^", "exp").replace("e**", "exp"),
                    {"x": value, "sin": math.sin, "cos": math.cos, "tan": math.tan, "exp": math.exp, "log": math.log,
                     "^": "**"})

    def significantFigure(self, n):
        if n == 0:
            return 0
        return round(n, - int(math.floor(math.log10(abs(n)))) + (self.percisionValue - 1))


class RaphsonFirstMethod():
    def __init__(self):
        print("im in raphson")
        self.toplevel_window = None
        self.intial_guessValue = None
        self.iterationsValue = None
        self.multiplicityValue = None

    def getPoints(self, root, equation, stepsButton):

        self.root = root
        self.stepsButton = stepsButton

        frame = Frame(root, bg="#222325")
        frame.pack(fill=tkinter.X)

        self.equation = equation
        self.multiplicity = customtkinter.CTkEntry(frame, placeholder_text="multiplicity", font=myFont,
                                                   border_color="#2C74B3")
        self.intial_guess = customtkinter.CTkEntry(frame, placeholder_text="Xo", font=myFont, border_color="#2C74B3")
        self.multiplicity.pack(fill=tkinter.X, side=tk.LEFT, anchor=NW, expand=True, padx=10, pady=5)
        self.intial_guess.pack(fill=tkinter.X, side=tk.LEFT, anchor=NW, expand=True, padx=10, pady=5)

        new_frame = Frame(root, bg="#222325")
        new_frame.pack(fill=tkinter.X)

        self.iterations = customtkinter.CTkEntry(new_frame, placeholder_text="Iteration number", font=myFont,
                                                 border_color="#2C74B3")
        self.tolerance = customtkinter.CTkEntry(new_frame, placeholder_text="Tolerance", font=myFont,
                                                border_color="#2C74B3")
        self.percision = customtkinter.CTkEntry(new_frame, placeholder_text="Percision", font=myFont,
                                                border_color="#2C74B3")
        self.iterations.pack(fill=tkinter.X, side=tk.LEFT, anchor=NW, expand=True, padx=10, pady=5)
        self.tolerance.pack(fill=tkinter.X, side=tk.LEFT, anchor=NW, expand=True, padx=10, pady=5)
        self.percision.pack(fill=tkinter.X, side=tk.LEFT, anchor=NW, expand=True, padx=10, pady=5)

        self.submitPoints = customtkinter.CTkButton(new_frame, text="Submit", command=self.submitPointsEvent, width=70,
                                                    font=importantFont)
        self.submitPoints.pack(padx=10, pady=5)

    def showSteps(self):
        print(self.toplevel_window is None)
        if self.toplevel_window is None or not self.toplevel_window.winfo_exists():
            self.toplevel_window = ToplevelWindow(self.root)  # create window if its None or destroyed
        else:
            self.toplevel_window.focus()  # if window exists focus it

        self.toplevel_window.title("Solution Steps")

        self.textbox = customtkinter.CTkTextbox(master=self.toplevel_window, width=400, corner_radius=0)
        self.textbox.pack(fill=tkinter.BOTH, expand=True)
        self.textbox.insert("0.0", self.steps)

    def submitPointsEvent(self):
        ###
        self.steps = ''
        self.stepsButton.configure(state='enabled')
        self.stepsButton.configure(command=self.showSteps)

        try:
            self.multiplicityValue = float(self.multiplicity.get()) if self.multiplicity.get() else 1
            self.intial_guessValue = float(self.intial_guess.get())
            self.iterationsValue = int(self.iterations.get())
            self.toleranceValue = float(self.tolerance.get()) if self.tolerance.get() else 0.00001
            self.percisionValue = int(self.percision.get()) if self.percision.get() else 7

        except ValueError:
            CTkMessagebox(title="Error", message="Enter valid Input", icon="cancel", font=importantFont)

        if (self.intial_guessValue is not None) and (self.iterationsValue is not None):
            self.perform_newton_first_method()

    def significantFigure(self, n):
        if n == 0:
            return 0.0
        return round(n, - int(math.floor(math.log10(abs(n)))) + (self.percisionValue - 1))

    def perform_newton_first_method(self):
        x = sp.symbols('x')
        made_equation = self.equation.replace("e^", "exp").replace("e**", "exp")
        fx = sp.sympify(made_equation)
        x_i = self.intial_guessValue
        max_iterations = self.iterationsValue
        error = self.toleranceValue
        dfx = fx.diff(x)
        self.steps = ""
        startTime = time.time()
        for i in range(max_iterations):
            fx_value = self.significantFigure(fx.subs(x, x_i))
            dfx_value = self.significantFigure(dfx.subs(x, x_i))
            x_i = self.significantFigure(x_i)
            if (fx_value == 0):
                self.steps += f"Root is equal to {x_i} with error = {error} and in {i + 1} iterations\n"
                endTime = time.time()
                totalTime = endTime - startTime
                CTkMessagebox(
                    message=f"Root is equal to {x_i} with error = {error} and in {i + 1} iterations" + f"with total time = {totalTime}",
                    icon="check")
                return
            try:
                x_i_next = self.significantFigure(x_i - self.multiplicityValue * (fx_value) / dfx_value)
            except ZeroDivisionError as e:
                CTkMessagebox(
                    title="Error",
                    message="Division by zero occurred, error tolerance can't be achieved, see steps for more info",
                    icon="cancel",
                    font=importantFont)
                return

            error = self.significantFigure(abs(x_i_next - x_i))
            x_i = self.significantFigure(x_i)
            self.steps += f"Iteration {i + 1}:\nXᵢ = {x_i},\nf(x) = {fx_value},\ndf(x) = {dfx_value},\nXᵢ₊₁ = {x_i_next},\nerror = {error}\n\n\n"

            if error < self.toleranceValue:
                endTime = time.time()
                totalTime = endTime - startTime
                self.steps += f"Root is approximately equal to {x_i_next} with error {error} and in {i + 1} iterations\n"
                CTkMessagebox(
                    message=f"Root is approximately equal to {x_i_next} with error {error} and in {i + 1} iterations" + f" with total time = {totalTime}",
                    icon="check")
                return

            x_i = float(x_i_next)

        x_i = self.significantFigure(x_i)
        error = self.significantFigure(error)
        endTime = time.time()
        totalTime = endTime - startTime
        CTkMessagebox(title="Error",
                      message=f"Couldn't converge within the specified iterations until reached x = {x_i} with error tolerance {error}"f" with total time = {totalTime}",
                      icon="cancel",
                      font=importantFont
                      )


class RaphsonSecondMethod():
    def __init__(self):
        self.toplevel_window = None
        self.iterationsValue = None
        self.intial_guessValue = None
        print("im in raphson")
        pass

    def getPoints(self, root, equation, stepsButton):
        self.root = root
        self.stepsButton = stepsButton

        frame = Frame(root, bg="#222325")
        frame.pack(fill=tkinter.X)

        self.equation = equation
        self.intial_guess = customtkinter.CTkEntry(frame, placeholder_text="Xo", font=myFont, border_color="#2C74B3")
        self.intial_guess.pack(fill=tkinter.X, side=tk.LEFT, anchor=NW, expand=True, padx=10, pady=5)

        new_frame = Frame(root, bg="#222325")
        new_frame.pack(fill=tkinter.X)

        self.iterations = customtkinter.CTkEntry(new_frame, placeholder_text="Iteration number", font=myFont,
                                                 border_color="#2C74B3")
        self.tolerance = customtkinter.CTkEntry(new_frame, placeholder_text="Tolerance", font=myFont,
                                                border_color="#2C74B3")
        self.percision = customtkinter.CTkEntry(new_frame, placeholder_text="Percision", font=myFont,
                                                border_color="#2C74B3")
        self.iterations.pack(fill=tkinter.X, side=tk.LEFT, anchor=NW, expand=True, padx=10, pady=5)
        self.tolerance.pack(fill=tkinter.X, side=tk.LEFT, anchor=NW, expand=True, padx=10, pady=5)
        self.percision.pack(fill=tkinter.X, side=tk.LEFT, anchor=NW, expand=True, padx=10, pady=5)

        self.submitPoints = customtkinter.CTkButton(new_frame, text="Submit", command=self.submitPointsEvent, width=70,
                                                    font=importantFont)
        self.submitPoints.pack(padx=10, pady=5)

    def showSteps(self):
        if self.toplevel_window is None or not self.toplevel_window.winfo_exists():
            self.toplevel_window = ToplevelWindow(self.root)  # create window if its None or destroyed
        else:
            self.toplevel_window.focus()  # if window exists focus it

        self.toplevel_window.title("Solution Steps")

        self.textbox = customtkinter.CTkTextbox(master=self.toplevel_window, width=400, corner_radius=0)
        self.textbox.pack(fill=tkinter.BOTH, expand=True)
        self.textbox.insert("0.0", self.steps)

    def submitPointsEvent(self):

        ###
        self.steps = ''
        self.stepsButton.configure(state='enabled')
        self.stepsButton.configure(command=self.showSteps)

        try:
            self.intial_guessValue = float(self.intial_guess.get())
            self.iterationsValue = int(self.iterations.get())
            self.toleranceValue = float(self.tolerance.get()) if self.tolerance.get() else 0.00001
            self.percisionValue = int(self.percision.get()) if self.percision.get() else 7

        except ValueError:
            CTkMessagebox(title="Error", message="Enter valid Input", icon="cancel", font=importantFont)

        if (self.intial_guessValue is not None) and (self.iterationsValue is not None):
            self.perform_newton_second_method()

    def significantFigure(self, n):
        if n == 0:
            return 0.0
        return round(n, - int(math.floor(math.log10(abs(n)))) + (self.percisionValue - 1))

    def perform_newton_second_method(self):
        x = sp.symbols('x')
        made_equation = self.equation.replace("e^", "exp").replace("e**", "exp")
        fx = sp.sympify(made_equation)
        x_i = self.intial_guessValue
        max_iterations = self.iterationsValue
        error = self.toleranceValue
        dfx = fx.diff(x)
        ddfx = dfx.diff(x)
        self.steps = ""
        startTime = time.time()
        for i in range(max_iterations):
            fx_value = self.significantFigure(fx.subs(x, x_i))
            dfx_value = self.significantFigure(dfx.subs(x, x_i))
            ddfx_value = self.significantFigure(ddfx.subs(x, x_i))
            x_i = self.significantFigure(x_i)
            if (fx_value == 0):
                self.steps += f"Root is equal to {x_i} with error = {error} and in {i + 1} iterations\n"
                endTime = time.time()
                totalTime = endTime - startTime
                CTkMessagebox(
                    message=f"Root is equal to {x_i} with error = {error} and in {i + 1} iterations" + f" with total time = {totalTime}",
                    icon="check")
                return
            try:
                x_i_next = self.significantFigure(x_i - (fx_value * dfx_value) / ((dfx_value ** 2) - fx_value * ddfx_value))
            except Exception as e:
                CTkMessagebox(
                    title="Error",
                    message="Division by zero occurred, error tolerance can't be achieved, see steps for more info",
                    icon="cancel",
                    font=importantFont)
                return

            error = self.significantFigure(abs(x_i_next - x_i))
            x_i = self.significantFigure(x_i)

            self.steps += (f"Iteration {i + 1}:\nXᵢ = {x_i},\nf(x) = {fx_value},\ndf(x) = {dfx_value},\nddf(x) = {ddfx_value},\nXᵢ₊₁ = {x_i_next},\nerror = {error}\n\n\n")

            if error < self.toleranceValue:
                endTime = time.time()
                totalTime = endTime - startTime
                self.steps += f"Root is approximately equal to {x_i_next} with error {error} and in {i + 1} iterations\n"
                CTkMessagebox(
                    message=f"Root is approximately equal to {x_i_next} with error {error} and in {i + 1} iterations" + f" with total time = {totalTime}",
                    icon="check")
                return

            x_i = float(x_i_next)

        x_i = self.significantFigure(x_i)
        error = self.significantFigure(error)
        endTime = time.time()
        totalTime = endTime - startTime
        CTkMessagebox(title="Error",
                      message=f"Couldn't converge within the specified iterations until reached x = {x_i} with error tolerance {error}"f" with total time = {totalTime}",
                      icon="cancel",
                      font=importantFont)


class App():
    def __init__(self, root):
        frame = Frame(root, bg="#222325")
        frame.pack(fill=tkinter.X)
        self.method = None
        self.factory = None
        self.comboBox = customtkinter.CTkComboBox(frame, command=self.combobox_callback,
                                                  state='readonly', values=METHODS,
                                                  border_color="#2C74B3", button_color="#2C74B3",
                                                  button_hover_color="#11426c", dropdown_hover_color="#11426c",
                                                  hover=True, font=myFont, dropdown_font=myFont)
        self.comboBox.set("Select Method")

        self.entry = customtkinter.CTkEntry(frame, placeholder_text="Enter the equation to find its roots",
                                            font=myFont, border_color="#2C74B3")

        self.comboBox.pack(fill=tkinter.X, padx=10, pady=10)
        self.entry.pack(side=tk.LEFT, fill=tkinter.X, anchor=NW, expand=True, padx=10, pady=5)

        self.submitEquation = customtkinter.CTkButton(frame, text="Enter", command=self.button_event, width=70,
                                                      font=importantFont)
        self.submitEquation.pack(side=tk.RIGHT, anchor=NE, expand=True, padx=10, pady=5)

        ###
        self.stepsButton = customtkinter.CTkButton(master=frame, text="Show steps", font=importantFont)
        self.stepsButton.pack(side=tkinter.RIGHT, anchor=NE, padx=10, pady=5)
        self.stepsButton.configure(state='disabled')

        self.quitButton = customtkinter.CTkButton(master=root, text="Restart", command=self._restart,
                                                  font=importantFont)
        self.quitButton.pack(side=tkinter.BOTTOM, padx=10, pady=10)

    def combobox_callback(self, choice):
        print("combobox dropdown clicked:", choice)
        self.factory = MethodFactory()
        self.method = self.factory.getMethod(choice)

    def button_event(self):
        print(self.validFn())
        if ((self.method != None) and (self.entry.get() != None) and
                (self.entry.get().lower().__contains__("x")) and (self.validFn() == True)):

            newplot = Plot(root).plotGraph(self.entry.get().lower())

            ###
            self.method.getPoints(root, self.entry.get().lower(), self.stepsButton)

            self.comboBox.configure(state='disabled')
            self.entry.configure(state='disabled')
            self.submitEquation.configure(state='disabled')

        elif (self.method == None):
            CTkMessagebox(title="Error", message="Select a method!", icon="cancel", font=importantFont)
        else:
            CTkMessagebox(title="Error", message="Enter a valid function!", icon="cancel", font=importantFont)

    def _restart(self):
        python = sys.executable
        os.execl(python, python, *sys.argv)

    def validFn(self):
        x = 10  # any number to test if valid
        try:
            eval(self.entry.get().lower().replace("e^", "exp").replace("e**", "exp"),
                 {"x": 10, "sin": math.sin, "cos": math.cos, "tan": math.tan, "exp": math.exp, "log": math.log,
                  "^": "**"})
            return True
        except:
            return False


root = customtkinter.CTk()
root.title("Root finder")
root.minsize(600, 600)
root.geometry('600x600')
root.resizable(True, True)
# root.iconbitmap("favicon2.ico")
customtkinter.set_appearance_mode("dark")
customtkinter.set_default_color_theme("dark-blue")
myFont = customtkinter.CTkFont(family='Poppins Light', weight='normal')
importantFont = customtkinter.CTkFont(family='Poppins Light', weight='bold')

# ctk_textbox_scrollbar = customtkinter.CTkScrollbar(root)
# ctk_textbox_scrollbar.pack(side= RIGHT, fill= BOTH)


App(root)


# ttk.Label(root, text='hiiiiiiiii').pack()
# countryVar = tkinter.StringVar()
# country = ttk.Combobox(root, textvariable="hello")
# country['state'] = 'readonly'
# country['values'] = ('USA', 'Canada', 'Australia')
# country.pack(fill=tkinter.X, padx=5, pady=5) #lazem tkon a5er haga


class Plot(metaclass=Singleton):
    def __init__(self, root):
        frame = Frame(root, bg="#222325")
        frame.pack(fill=tkinter.X)
        self.points = np.linspace(-20, 20, 1000)
        self.figure = Figure(figsize=(5, 4))
        self.canvas = FigureCanvasTkAgg(self.figure, master=frame)  # A tk.DrawingArea.
        self.canvas.get_tk_widget().pack(side=tkinter.TOP, fill=tkinter.BOTH, expand=1)
        self.toolbar = NavigationToolbar2Tk(self.canvas, frame)
        self.canvas.get_tk_widget().pack(side=tkinter.TOP, fill=tkinter.BOTH, expand=1, padx=10, pady=10)

    def plotGraph(self, equation):
        x = self.points
        self.canvas.get_tk_widget().delete("all")

        self.fn = self.evaluateFunction(equation)
        try:
            ###
            ax = self.figure.add_subplot()
            ax.grid(True, linestyle='--', linewidth=0.5)
            ax.plot(self.points, eval(self.fn))

            self.canvas.draw()
            self.canvas.mpl_connect("key_press_event", self.on_key_press)
            # self.figure.add_subplot().plot(self.points, eval(self.fn))
        except:
            CTkMessagebox(title="Error", message="Enter a valid function !", icon="cancel", font=importantFont)

    def evaluateFunction(self, equation):
        eqn = equation.replace(" ", "").lower()
        eqn = eqn.replace("e^", "np.exp").replace("e**", "np.exp").replace("^", "**").replace("sin", "np.sin").replace(
            "cos", "np.cos").replace("tan", "np.tan")
        print(eqn)
        return eqn

    def on_key_press(self, event):
        print("you pressed {}".format(event.key))
        key_press_handler(event, self.canvas, self.toolbar)


###
class ToplevelWindow(customtkinter.CTkToplevel):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.geometry("400x300")
        self.label = customtkinter.CTkLabel(self, text="Solution Steps", font=importantFont)
        self.label.pack(padx=20, pady=20)


root.mainloop()
# If you put root.destroy() here, it will cause an error if the window is
# closed with the window manager.